open Ast

let string_of_func_body = function
  | Map m ->
      List.fold_right (fun (k, v) acc -> "(" ^ k ^ " " ^ v ^ ") " ^ acc) m ""
      |> Printf.printf "\t%s\n"

let print_entry = function
  | Field (name, values) -> 
      let vs = List.fold_right (fun x acc -> x ^ " " ^ acc) values "" in
      Printf.printf "\t%s: %s\n" name vs
  | Message (message, v) ->
      let ty, field =
        match v with
        | Message_fields (ty, field) -> 
          (handle_ty ty, field) in
      Printf.printf "\t%s: %s (%s)\n" message field ty

let print_statement = function
  | Fields es ->
      print_string "Fields:\n";
      List.iter print_entry es
  | Messages es ->
      print_string "Messages:\n";
      List.iter print_entry es
  | Function (name, ty, body) ->
    let ty_string = 
      match ty with
      | Some (a, b) -> a ^ " -> " ^ b
      | None -> "no type" in
    Printf.printf "Function %s of %s:\n" name ty_string;
    string_of_func_body body

let print_program = function
  (fields, messages, functions) ->
    begin 
      (match fields with
      | Some fs -> print_statement fs
      | None    -> ());

      (match messages with
      | Some ms -> print_statement ms
      | None    -> ());

      List.iter print_statement functions
    end


let () =
  let lexbuf = Lexing.from_channel stdin in
  let ast =
    try Grammar.program Lexer.lexer lexbuf with
    | Lexer.LexingError msg ->
        let n = string_of_int !Lexer.num_lines in
        Printf.eprintf "Line %s: %s" n msg;
        exit (-1)
    | Grammar.Error ->
        string_of_int !Lexer.num_lines
        |> Printf.eprintf "Syntax Error at line %s";
        exit (-1)
    in
  Semantics.check_program ast
  |> print_program;
