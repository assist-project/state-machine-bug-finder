%{
  open Ast
%}

%token T_arrow     "->"
%token T_colon     ":"
%token T_comma     ","
%token T_equals    "="
%token T_lbracket  "["
%token T_lcbracket "{" (* curly bracket *)
%token T_rbracket  "]"
%token T_rcbracket "}"
%token<string> T_word

%token T_fields 
%token T_pmessages
%token T_func 
%token T_mapping

%token T_eof


%start <statement option * statement option * statement list> program

%%

let program      := ~ = fields_def?; ~ = messages_def?; ~ = func*; T_eof;          <>

let fields_def   := T_fields; "="; ~ = field+;                                     < Fields >

let field        := ~ = T_word; "->"; ~ = field_values;                            < Field >

let field_values := ~ = separated_nonempty_list(",", T_word);                      <>


let messages_def := T_pmessages; "="; ~ = message+;                                < Messages >

let message      := ~ = separated_pair(T_word, "->", message_val);                 < Message >

let message_val  := v = delimited("[", T_word, "]");                               { Message_val (List, v) }
                  | v = delimited("{", T_word, "}");                               { Message_val (Set, v) }

let func         := T_func; ~ = T_word; ":"; ~ = func_ty; "="; ~ = func_body;      < Function >

let func_ty      := T_mapping;                                                     { Mapping }

let func_body    := ~ = separated_pair(T_word, "->", T_word)*;                     < Map >
