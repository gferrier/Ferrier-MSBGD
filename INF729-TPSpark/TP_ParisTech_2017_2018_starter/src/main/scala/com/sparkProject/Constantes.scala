package com.sparkProject


import org.apache.spark.ml.feature.StopWordsRemover

object Constantes {
  val sourceDataParquet = "/cal/homes/gferrier/Downloads/prepared_trainingset"

  // Le nom des colonnes exploitées par la transfo
  val colonneDescParquet = "text" // la colonne de sortie du preprocesseur qui a nettoyé
  val colonneCountryParquet = "country2" // on a du nettoyer la colonne par fusion de country et currency ...
  val colonneCurrencyParquet = "currency2" // on a du nettoyer la currency ...
  val colonneDaysCampaignParquet = "days_campaign"
  val colonneHoursPrepa = "hours_prepa"
  val colonneGoal = "goal"
  val colonneFinalStatus = "final_status"

  // hyper parametres à tester
  val arrayLambda = Array(1.0e-8,1.0e-6,1.0e-4,1.0e-2)
  val arrayMinDF = Array(55.0,75.0,95.0)
  // arggg non le split de la validation est fait en interne dans le trainValidationSplit donc on veut un vrai 90-10
  //val splitCorpus = Array(0.63,0.27,0.1)
  val splitCorpus = Array(0.9,0.1)
  val validatorMetric = "f1" // supports "f1","weightedPrecision","weightedRecall","accuracy"
  val validatorTrainRatio = 0.7

  // les variables magic-numbers des bibliotheques de transfo/apprentissage
  // Celles a definir absolument

  //  var stopwords = Array("a","the","s","and","an","if","i","of")
  val stopwords = StopWordsRemover.loadDefaultStopWords("english")
  val minDocFreq = 5 //int // nb projets qui ont ce mot : si on a pas assez de corpus pour ce mot ca ne veut rien dire

  // Celles a priori inutilisées mais présentes dans du code possible de décommenter :
  // INUTILISE : valeur par defaut dans le code
  val minTF = 1.0 //double // tous les mots sont importants non ?
  // < 1 : frequence min donc prendre presque 0
  // > 1 : count threshold
  // 1.0 : on garde tout
  // INUTILISE : valeur par defaut dans le code
  val vocabSize = 10000 //int // limite de la taille du vocab

}
