package com.sparkProject

import org.apache.spark.SparkConf
import org.apache.spark.ml.classification.LogisticRegression
import org.apache.spark.ml.feature._
import org.apache.spark.ml.evaluation._
import org.apache.spark.ml.tuning._
import org.apache.spark.sql.SparkSession
import org.apache.spark.ml.Pipeline


object Trainer {

  def main(args: Array[String]): Unit = {

    val conf = new SparkConf().setAll(Map(
      "spark.scheduler.mode" -> "FIFO",
      "spark.speculation" -> "false",
      "spark.reducer.maxSizeInFlight" -> "48m",
      "spark.serializer" -> "org.apache.spark.serializer.KryoSerializer",
      "spark.kryoserializer.buffer.max" -> "1g",
      "spark.shuffle.file.buffer" -> "32k",
      "spark.default.parallelism" -> "12",
      "spark.sql.shuffle.partitions" -> "12",
      "spark.driver.maxResultSize" -> "2g"
    ))

    val spark = SparkSession
      .builder
      .config(conf)
      .appName("TP_spark")
      .getOrCreate()


    /*******************************************************************************
      *
      *       TP 3
      *
      *       - lire le fichier sauvegarder précédemment
      *       - construire les Stages du pipeline, puis les assembler
      *       - trouver les meilleurs hyperparamètres pour l'entraînement du pipeline avec une grid-search
      *       - Sauvegarder le pipeline entraîné
      *
      *       if problems with unimported modules => sbt plugins update
      *
      ********************************************************************************/

    val df = spark.read.parquet(Constantes.sourceDataParquet)

    // // pour rappel du contenu ...
    // df.show()
    // df.printSchema()


    // stages du pipeline
    // 1st Stage tokenizer : split des mots
    val tokenizer = new RegexTokenizer()
        .setPattern("\\W+")
          .setGaps(true)
          .setInputCol(Constantes.colonneDescParquet) // ICI LA colonne d'entree du pipeline
          .setOutputCol("tokens") // peu importe les noms des outputcol vu qu on utilisera des getoutputcol ...

    // 2nd stage StopWords (elimination des mots inutiles)
    val remover = new StopWordsRemover()
      .setStopWords(Constantes.stopwords)
      .setInputCol(tokenizer.getOutputCol)
      .setOutputCol("withoutStop")

    // 3rd stage partie TF
    val vectorizer = new CountVectorizer()
 //     .setMinTF(Constantes.minTF) // minimum par defaut à 1 ok
 //     .setVocabSize(Constantes.vocabSize) // pas de limite
      .setInputCol(remover.getOutputCol)
      .setOutputCol("countVect")

    // 4th stage partie IDF
    val tfidf = new IDF()
      .setMinDocFreq(Constantes.minDocFreq)
      .setInputCol(vectorizer.getOutputCol)
      .setOutputCol("tfidf")


    // 5th stage conversion indexer de country
    val stridxcountry = new StringIndexer()
          .setInputCol(Constantes.colonneCountryParquet)
          .setOutputCol("country_indexed")

    // 6th stage conversion indexer de currency
    val stridxcurrency = new StringIndexer()
      .setInputCol(Constantes.colonneCurrencyParquet)
      .setOutputCol("currency_indexed")

    // 7th 8th stage one hot encoder
    // en fait le vrai truc qu'on voulait : 5th et 6th ne sont que du cast de format parceque onhot prends une entree chiffre et pas string
    val onehotcountry = new OneHotEncoder()
      .setInputCol(stridxcountry.getOutputCol)
      .setOutputCol("country_onehot")

    val onehotcurrency= new OneHotEncoder()
      .setInputCol(stridxcurrency.getOutputCol)
      .setOutputCol("currency_onehot")

    // 9th definir les colonnes utiles (transfo equivalent de la fonction struct sur un dataframe)
    val features = new VectorAssembler()
      .setInputCols(Array(tfidf.getOutputCol,Constantes.colonneDaysCampaignParquet,
        Constantes.colonneHoursPrepa,Constantes.colonneGoal,onehotcountry.getOutputCol, onehotcurrency.getOutputCol
      ))
      .setOutputCol("features")

    // 10th le modele de classification
    val lr = new LogisticRegression()
      .setElasticNetParam(0.0)
      .setFitIntercept(true)
      .setFeaturesCol(features.getOutputCol)
      .setLabelCol(Constantes.colonneFinalStatus)
      .setStandardization(true)
      .setPredictionCol("predictions")
      .setRawPredictionCol("raw_predictions")
      .setThresholds(Array(0.7,0.3))
      .setTol(1.0e-6)
      .setMaxIter(300)

    // Le Pipeline des 10 etapes
    val pl = new Pipeline().setStages(Array(tokenizer,remover,vectorizer, tfidf,
      stridxcountry,stridxcurrency,onehotcountry,onehotcurrency,features ,lr
    ))

    // etape 5.k split du corpus en 90/10 puis le 90 en 70/30 donc au final en 0.63,0.27,0.10 !! pourquoi 2 etapes ??
    // arggg non le split de la validation est fait en interne dans le trainValidationSplit donc on veut un vrai 90-10
    //val Array(dftrain,dfvalid,dftest) = df.randomSplit(Constantes.splitCorpus)
    val Array(dftrain,dftest) = df.randomSplit(Constantes.splitCorpus)

    // etape 5.l lacement sur grille
    // evaluator
    val mcef1 = new MulticlassClassificationEvaluator()
      .setMetricName(Constantes.validatorMetric) // supports "f1","weightedPrecision","weightedRecall","accuracy"
      .setLabelCol(lr.getLabelCol)
      .setPredictionCol(lr.getPredictionCol)

    val paramGrid = new ParamGridBuilder()
      .addGrid(lr.regParam, Constantes.arrayLambda)
      .addGrid(vectorizer.minDF, Constantes.arrayMinDF)
      .build()

    val runMeasures = new TrainValidationSplit()
      .setEstimator(pl)
      .setEvaluator(mcef1)
      .setEstimatorParamMaps(paramGrid)
      .setTrainRatio(Constantes.validatorTrainRatio)

    // L apprentissage
    //val model = pl.fit(dftrain)
    val modelValide = runMeasures.fit(dftrain)
    // ok il le fait pour tous mais pourquoi on n'a pas acces aux mesures comment on fait pour savoir quels etaient les bons hyperparams ??

    // etape 5.m test on test data
    val df_WithPredictions = modelValide.transform(dftest)

    println("### Evaluation sur jeu de test = ", mcef1.evaluate(df_WithPredictions))
    // 0.635 un peu decevant, ok c'est mieux que 0.5 MAIS moins bien que de prendre la classe majoritaire
    // on predirait 0 tout le temps on aurait un meilleur score ! mais du coup on passerait à coté des bons plans

    // etape 5.n  affichage compteurs selons les couples prevu/obtenu
    df_WithPredictions.groupBy(lr.getLabelCol,lr.getPredictionCol).count.show()
    
    //+------------+-----------+-----+                                                
    //|final_status|predictions|count|
    //+------------+-----------+-----+
    //|           1|        0.0|  985| 
    //|           0|        1.0| 2947|
    //|           1|        1.0| 2449|
    //|           0|        0.0| 4417|
    //+------------+-----------+-----+
    //  // si le but est d'avoir un minimum de faux negatif (passer à coté d une aubaine) on n'est pas si mal mais c'est quand meme decevant
    //  // si le but est d'avoir un minimum de faux positifs (cashflow immobilisé pour rien) c'est deja un peu moins mien
    //  // si le but est d'etre "juste" c'est tout pourri par rapport à la classe majoritaire.
  
    // sauvegarde du model
    modelValide.write.save("GFE-KickstartLogisticRegression")
  }
}
