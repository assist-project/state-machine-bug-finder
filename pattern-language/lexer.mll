{
    open Grammar

    let num_lines = ref 0

    exception LexingError of string
}

(* definitions of useful regexps *)
let word     = ['a'-'z' 'A'-'Z' '0'-'9' '_']+
let white  = [' ' '\t' '\r']

rule lexer = parse
  | "->"        { T_arrow }
  | ":"         { T_colon }
  | ","         { T_comma }
  | "="         { T_equals }
  | "["         { T_lbracket }
  | "]"         { T_rbracket }
  | "{"         { T_lcbracket }
  | "}"         { T_rcbracket }
  | "mapping"   { T_mapping }

  | "fields"    { T_fields }
  | "parametric_messages"  { T_pmessages }
  | "func"      { T_func }

  | word            { T_word (Lexing.lexeme lexbuf) } 

    (* to be ignored by parser: white chars and comments *)
  | '\n'      { incr num_lines; lexer lexbuf}
  | white+    { lexer lexbuf    }

  |  eof      { T_eof }

  | "//"      { comment lexbuf  }
  | "/*"      { mcomment lexbuf }

  |  _ as c   { raise (LexingError ("Invalid character: " ^ (String.make 1 c) ^ " (ASCII: " ^ string_of_int (Char.code c) ^ ")")) }

and comment = parse
  | '\n'       { incr num_lines; lexer lexbuf }
  | _          { comment lexbuf               }

and mcomment = parse
  | "*/"       { lexer lexbuf                    }
  | '\n'       { incr num_lines; mcomment lexbuf }
  | _          { mcomment lexbuf                 }
  | eof        { raise (LexingError "Lexing Error -> Reached EOF: Missing closing '*/'\n") }

{
  let string_of_token token =
    match token with
    | T_arrow     -> "->"
    | T_colon     -> ":"
    | T_comma     -> ","
    | T_equals    -> "="
    | T_lbracket  -> "["
    | T_lcbracket -> "{"
    | T_rbracket  -> "]"
    | T_rcbracket -> "}"
    | T_word _    -> "T_word"

    | T_fields    -> "Fields_def"
    | T_pmessages -> "Pmessages_def"
    | T_func      -> "Func_def"
    | T_mapping   -> "map_func_type"

    | T_eof       -> "T_eof"
}
