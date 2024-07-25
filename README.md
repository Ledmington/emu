# Emu - a processor emulator.
![Build](https://github.com/Ledmington/emu/actions/workflows/build.yaml/badge.svg)

The project is divided as follows:
- `emu-cli` : the actual executable emulator (CLI version)
- `emu` : the core components of the emulator
- `readelf` : a utility executable to mimic the behavior of GNU's `readelf`
- `elf` : ELF file parser
- `id` : Instruction Decoder (currently only X86)
- `mem` : implementation of various emulated memories
- `utils` : various utility components

## How to build
### Linux
This command formats the code, compiles and creates a single executable jar with all the dependencies inside for each of the executables provided.
```bash
./gradlew fatJar
```

### Windows
This command formats the code, compiles and creates a single executable jar with all the dependencies inside for each of the executables provided.
```batch
gradlew.bat fatJar
```

## How to contribute
### Linux
```bash
./gradlew build
```
This command is equivalent to:
```bash
./gradlew spotlessApply pmdMain pmdTest spotbugsMain spotbugsTest test javadoc fatJar
```

### Windows
```bash
gradlew.bat build
```
This command is equivalent to:
```bash
gradlew.bat spotlessApply pmdMain pmdTest spotbugsMain spotbugsTest test javadoc fatJar
```

## License
Currently the project is licensed under the GNU General Public License v3.
