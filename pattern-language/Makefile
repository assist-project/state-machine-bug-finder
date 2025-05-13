# Usage:
#   `make libstubs.so`    -> dynamic library for Linux
#   `make libstubs.dylib` -> generate dynamic library for macOS
#   `make clean`          -> delete files produced by compilation of files, but keep the shared library
#   `make distclean`      -> delete all files produced
#   `make exec`           -> compile ocaml into an executable file


.PHONY: clean distclean exec

OCAML_LIB = `ocamlc -where`


# ocaml compilation
ast.cmx: ast.ml
	ocamlopt -c ast.ml

grammar.ml grammar.mli: grammar.mly ast.cmx
	menhir --infer --no-dollars grammar.mly

lexer.ml: lexer.mll
	ocamllex lexer.mll

main.o : functions_java.ml ast.cmx grammar.ml grammar.mli lexer.ml
	ocamlopt -output-obj -o main.o ast.cmx grammar.mli grammar.ml lexer.ml functions_java.ml 


# stubs and shared library compilation
libstubs.so: stubs.c main.o
	clang -shared -Wall -o libstubs.so stubs.c main.o \
		-I$(OCAML_LIB) \
		-I$(JAVA_HOME)/include \
		-I$(JAVA_HOME)/include/linux \
		-L$(OCAML_LIB) -lasmrun_shared

libstubs.dylib: stubs.c main.o # maybe remove -shared from this
	clang -shared -Wall -o libstubs.dylib stubs.c main.o \
		-I$(OCAML_LIB) \
		-I$(JAVA_HOME)/include \
		-I$(JAVA_HOME)/include/darwin \
		-L$(OCAML_LIB) -lasmrun



# phony targets
exec: lexer.ml ast.cmx functions_java.ml grammar.ml
	ocamlopt -o main ast.cmx grammar.mli grammar.ml lexer.ml functions_java.ml

clean:
	rm -f *.cmi *.cmo *.cmx *.o *.class *.h *.log lexer.ml grammar.ml grammar.mli

distclean: clean
	rm -f *.dylib *.so
