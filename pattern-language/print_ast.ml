open Ast

let string_of_func_ty = function
  | Mapping -> "Mapping"

let string_of_func_body = function
  | Map m ->
      List.fold_right (fun (k, v) acc -> "(" ^ k ^ " " ^ v ^ ") " ^ acc) m ""

let print_entry = function
  | Field (name, values) -> 
      let x = List.fold_right (fun x acc -> x ^ " " ^ acc) values "" in
      print_string ("\t" ^ name ^ ": " ^ x ^ "\n")
  | Message ((message, field)) ->
      print_string ("\t" ^ message ^ ": " ^ field ^ "\n")

let print_statement = function
  | Fields es ->
      (print_string "Fields:\n";
      ignore (List.map (fun e -> print_entry e) es))
  | Messages es ->
      (print_string "Messages:\n";
      ignore (List.map (fun e -> print_entry e) es))
  | Function (name, ty, body) ->
    let ty_string = string_of_func_ty ty in
    let body_string = string_of_func_body body in
    (print_string ("Function " ^ name ^ " of " ^ ty_string ^ ":\n");
    print_string ("\t" ^ body_string ^ "\n"))


let print_program = function
  (fields, messages, functions) ->
    (List.map Option.get [fields; messages]) @ functions
    |> List.iter print_statement;