# rg-save-file-optimizer

###### Rogue Galaxy Save File Optimizer

`rg-save-file-optimizer` is a tool written in Java that is able to optimize Rogue Galaxy save files.

Its main use is to get the game not to display story text when loading the save file, effectively speeding up the
process of loading a save file. It is especially useful for speedrunners when practicing the game: they often have to
load saves over and over. Skipping story text will save them precious time.

The latest release can be found on the [releases page](https://github.com/piorrro33/rg-save-file-optimizer/releases).

## Usage

See `rg-save-file-optimizer -h` for the latest usage information. You may also drag and drop save files onto the
executable. A backup will be made (with the `.orig` extension), and the save file will be edited.

## Building

Execute `./gradlew build`.

## Running the tool

Execute `./gradlew run`.

## Generating a platform-specific executable

You may generate a platform-specific executable by following these steps:

- Install the prerequisites: https://www.graalvm.org/reference-manual/native-image/#prerequisites
- If you're building on Windows, create a file named `gradle.properties` at the root of the repository and add
  `vsVarsPath=pathToVcvars64.bat`
- Run `./gradlew nativeImage`

The executable with its symbols will be generated in `build/graal`.
