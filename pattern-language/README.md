# Parametric Language Support for Bug Patterns

Here, the parametric language implementation is provided in OCaml as well as stubs that enable Java to call the appropriate functions from within.

## Build Instructions

1. Install [opam](https://opam.ocaml.org) using your system's package manager
2. Follow the instructions provided by the opam package once it is installed, in order to initialize it.
3. Create an opam switch with ocaml 5.3.0 (i.e. `opam switch create pattern-language 5.3.0`)
4. Install the menhir package using opam (i.e. `opam install menhir`)

Now, ocaml should be installed and set up. As mentioned in the main README, make sure you have *clang* and environment variables *JAVA_HOME* and *LD_LIBRARY_PATH* are set.

You should be able to compile successfully using the *Makefile* provided here (i.e. `make libstubs.so` or `make libstubs.dylib` for Linux and macOS accordingly).
