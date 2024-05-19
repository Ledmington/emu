# Emu
![Build](https://github.com/Ledmington/emu/actions/workflows/build/badge.svg)

A processor emulator.

The project is divided as follows:
- `emu` : the actual emulator
- `readelf` : a utility executable to mimic the behavior of GNU's `readelf`
- `elf` : ELF file parser
- `id` : Instruction Decoder (currently only X86)
- `utils` : various utility components

## How to build
### Linux
This command formats the code, compiles and creates a single executable jar with all the dependencies inside.
```bash
./gradlew fatJar
```

### Window
This command formats the code, compiles and creates a single executable jar with all the dependencies inside.
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
./gradlew spotlessApply pmdMain pmdTest test javadoc fatJar
```

### Windows
```bash
gradlew.bat build
```
This command is equivalent to:
```bash
gradlew.bat spotlessApply pmdMain pmdTest test javadoc fatJar
```

## License
Currently the project is licensed under the GNU General Public License v3.
