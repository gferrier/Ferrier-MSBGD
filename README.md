# Ferrier-MSBGD
Bonjour ceci est le repository pour les éventuels TP à rendre dans le cadre du MS BGD

Le premier projet concerné est celui du dernier TP Spark.
Je reprends pour le moment ici ce qui ne devrait être que le readme de CE sous projet et non pas de tout le repository.

Le projer reprends le matériel fourni pour le TP à savoir le contenu de l'archive mais avec les sources mises à jour.
Pour le faire fonctionner il suffirait de récupérer n'importe quel environnement équivalent et de récupérer les fichiers
```
src/main/scala/com/sparkProject/Constantes.scala
src/main/scala/com/sparkProject/Trainer.scala
```
Pour le tester/faire fonctionner il y a besoin d'éditer la première variable du fichier 
``` Constantes.scala```
Pour y indiquer le chemin du fichier de données préprocéssé lors du TP2
```scala
val sourceDataParquet = "/cal/homes/gferrier/Downloads/prepared_trainingset"
```

Une fois cette modification faite il suffit d'utiliser les outils de compilation et lancement fournis qui supposent que SBT et spark soient disponibles :
```sh
build_and_submit.sh  Trainer
```
