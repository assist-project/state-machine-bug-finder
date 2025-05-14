

exception ParamLang of string

let get_ast path =
  Lexer.num_lines := 0;
  let input = open_in path in
  let lexbuf = Lexing.from_channel input in
  
  (try
    Grammar.program Lexer.lexer lexbuf
  with
  | Lexer.LexingError msg ->
      raise (ParamLang msg)
  | Grammar.Error ->
      let n = string_of_int !Lexer.num_lines in
      raise (ParamLang ("Syntax error in " ^ path ^ " at line " ^ n)))


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
