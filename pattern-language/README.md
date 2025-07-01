# Parametric Language Support for Bug Patterns

Here, the parametric language implementation is provided in OCaml as well as stubs that enable Java to call the appropriate functions from within.

## Build Instructions

1. Install [opam](https://opam.ocaml.org) using your system's package manager
2. Follow the instructions provided by the opam package once it is installed, in order to initialize it.
3. Create an opam switch with ocaml 5.3.0 (i.e. `opam switch create pattern-language 5.3.0`)
4. Install the menhir package using opam (i.e. `opam install menhir`)

Now, ocaml should be installed and set up. As mentioned in the main README, make sure you have _clang_ and environment variables _JAVA_HOME_ and _LD_LIBRARY_PATH_ are set.

You should be able to compile successfully using the _Makefile_ provided here (i.e. `make libstubs.so` or `make libstubs.dylib` for Linux and macOS accordingly).

## Documentation

### Grammar

The parser ([grammar.mly](grammar.mly)) implements the following grammar:

```
<program> ::= <fields_def>? <messages_def>? <func>*

<fields_def> ::= `fields=` <field>+

<field> ::= <word> `->` <field_values>

<field_values> ::= <word> (`,` <word>)*

<messages_def> ::= `parametric_messages=` <message>+

<message> ::= <word> <message_val>

<message_val> ::= `(` <word> `)` | `{` <word> `}`

<func> ::= `func` <word> <func_ty>? `=` <func_body>

<func_ty> ::= `:` <word> `->` <word>

<func_body> ::= (<word> `->` <word>)*
```

### Examples

Let's consider the parametric bug patterns in DTLS server:

[non-conforming_cookie.dot](../src/main/resources/patterns/dtls/server/parametric/non-conforming_cookie.dot)

```
digraph G {
  label=""
  init [color="red"]
  bug [shape="doublecircle"]

  init -> ch1 [label="I_CLIENT_HELLO[Xlist:=cipher_suites]"]
  init -> init [label="other"]

  ch1 -> hvr [label="O_HELLO_VERIFY_REQUEST"]

  hvr -> ch2 [label="I_CLIENT_HELLO[Ylist:=cipher_suites] where Ylist != Xlist"]

  ch2 -> bug [label="O_SERVER_HELLO"]

  __start0 [label="" shape="none" width="0" height="0"];
  __start0 -> init;
}
```

Using this syntax in DOT we have defined the parameters Xlist and Ylist on the I_CLIENT_HELLO message in transitions init -> ch1 and hvr -> ch2.
We have also defined that the transition hvr -> ch2 will only occur when these two parameters are not equal.
What remains is to define what values Xlist and Ylist can have. This will be done in a second file which is shown below.

[parameters.lang](../src/main/resources/patterns/dtls/server/parametric/parameters_for_test.lang)

```
fields =
    cipher_suites -> RSA, PSK, ECDH, DH

parametric_messages =
    CLIENT_HELLO {cipher_suites}
```

First, we have defined a set of values cipher_suites can have: `RSA, PSK, ECDH, DH`. And then we declare that parametric `CLIENT_HELLO` messages will take parametric values from cipher_suites, that is `RSA, PSK, ECDH, DH` as mentioned.
