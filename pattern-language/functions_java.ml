

exception ParamLang

(* These are for printing exception errors *)
let red = "\027[31m" (* ANSI escape code for red *)
let reset = "\027[0m" (* Reset to default color *)

let get_ast path =
  Lexer.num_lines := 0;
  let input = open_in path in
  let lexbuf = Lexing.from_channel input in
  
  try
    Grammar.program Lexer.lexer lexbuf
    |> Semantics.check_program 
  with
  | Lexer.LexingError msg ->
      Printf.eprintf "%s[%s] Lexing Error: %s%s\n" red path msg reset;
      raise ParamLang
  | Grammar.Error ->
      Printf.eprintf "%s[%s] Syntax Error at line %d%s\n" red path !Lexer.num_lines reset;
      raise ParamLang
  | Semantics.SemanticError msg ->
      Printf.eprintf "%s[%s] Semantic Error: %s%s\n" red path msg reset;
      raise ParamLang


let get_fields lang_path =
  get_ast lang_path
  |> Ast.get_fields

let get_messages_map lang_path =
  get_ast lang_path
  |> Ast.get_messages_map

let get_functions lang_path =
  get_ast lang_path
  |> Ast.get_functions

let () =
  Callback.register "ocaml_get_fields" get_fields;
  Callback.register "ocaml_get_messages_map" get_messages_map;
  Callback.register "ocaml_get_functions" get_functions
