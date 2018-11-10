# Ferrier-MSBGD

Le projer reprends le matériel fourni pour le TP à savoir le contenu de l'archive du projet "idea" mais avec les sources mises à jour.
Pour le faire fonctionner il suffirait de récupérer n'importe quel environnement équivalent et de récupérer les fichiers
```
src/main/scala/com/sparkProject/Constantes.scala
src/main/scala/com/sparkProject/Trainer.scala
```

Constante contiends la majorité des constantes relatives aux entrées ou aux paramètres à régler. C'est le cas si on travaille sur un fichier parquet dont le nom des colonnes n'est pas celui attendu.

Les autres variables qui ne devraient pas être modifiées sont en dur dans Trainer : cela inclus les colonnes de sorties de chaque étape du pipeline qui ne sont présent qu'une fois, et peuvent donc etre modifiées (les appels suivants se faisant via des getter sur les objets qui les ont définis)
Trainer est le livrable voulu qui contient les commandes du pipeline. C'est la classe à donner au script build_and_submit.

Pour le tester/faire fonctionner il y a SURTOUT besoin d'éditer la première variable du fichier 
``` Constantes.scala```
Pour y indiquer le chemin du fichier de données préprocéssé lors du TP2
```scala
val sourceDataParquet = "/cal/homes/gferrier/Downloads/prepared_trainingset"
```
NB : le repertoire contenant le contenu parquet est repris sur ce repository à la racine du projet il s'agit du répertoire prepared_trainingset

Une fois cette modification faite il suffit d'utiliser les outils de compilation et lancement fournis qui supposent que SBT et spark soient disponibles :
```sh
build_and_submit.sh  Trainer
```

