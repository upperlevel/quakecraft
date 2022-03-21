# Quakecraft

## Usage

You can find a usage documentation here: https://upperlevel.github.io/quakecraft/

## How to build

Navigate to the directory where you want to install Quakecraft and its dependencies.
If you're using IntelliJ Idea you may want to install them under `~/IdeaProjects`.

Clone and install [Uppercore](https://github.com/upperlevel/uppercore):
```
git clone -b 2.0.2 https://github.com/upperlevel/uppercore
cd uppercore
graldew install
```

Now you can clone and build Quakecraft:
```
git clone -b 2.1.5 https://github.com/upperlevel/quakecraft
cd quakecraft
gradlew build
```

You can find the Quakecraft .jar at the following path: `./build/libs/quakecraft-*-all.jar`.
