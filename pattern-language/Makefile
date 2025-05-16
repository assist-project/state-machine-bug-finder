# Usage:
#   `make libstubs.so`    -> dynamic library for Linux
#   `make libstubs.dylib` -> generate dynamic library for macOS
#   `make clean`          -> delete files produced by compilation of files, but keep the shared library
#   `make distclean`      -> delete all files produced
#   `make exec`           -> compile parser into an executable file that prints the ast (i.e. ./print_ast < input.txt)


.PHONY: clean distclean exec

OCAML_LIB = `ocamlc -where`


# ocaml compilation
ast.cmx: ast.ml
	ocamlopt -c $^

grammar.ml grammar.mli: grammar.mly ast.cmx
	menhir --infer --no-dollars $<

lexer.ml: lexer.mll
	ocamllex $<

main.o : ast.cmx grammar.mli grammar.ml lexer.ml semantics.ml functions_java.ml
	ocamlopt -output-obj -o $@ $^


# stubs and shared library compilation
libstubs.so: stubs.c main.o
	clang -shared -Wall -o $@ $^ \
		-I$(OCAML_LIB) \
		-I$(JAVA_HOME)/include \
		-I$(JAVA_HOME)/include/linux \
		-L$(OCAML_LIB) -lasmrun_shared

libstubs.dylib: stubs.c main.o # maybe remove -shared from this
	clang -shared -Wall -o $@ $^ \
		-I$(OCAML_LIB) \
		-I$(JAVA_HOME)/include \
		-I$(JAVA_HOME)/include/darwin \
		-L$(OCAML_LIB) -lasmrun



# phony targets
exec: ast.cmx grammar.mli grammar.ml lexer.ml semantics.ml print_ast.ml
	ocamlopt -o print_ast $^

clean:
	rm -f *.cmi *.cmo *.cmx *.o *.class *.h *.log lexer.ml grammar.ml grammar.mli

distclean: clean
	rm -f *.dylib *.so print_ast
