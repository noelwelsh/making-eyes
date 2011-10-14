# Deploying BlueEyes Applications

At some point you'll want to move your BlueEyes application to a live environment. Creating a `JAR` file containing the code and all it's dependencies is the simplest way to do this. Then you can just run

    java -jar myAwesomeApp.jar 
    
and it will Just Work. Installing the [OneJar SBT plugin](https://github.com/retronym/sbt-onejar/) is the simplest way to do this. 
