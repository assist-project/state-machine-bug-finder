open Either
open Option
open Ast

exception SemanticError of string

module Fields = Map.Make(String)

let check_f_type map = function
  | Some (a, b) -> Fields.mem a map && Fields.mem b map 
  | None -> true

let check_func_body valuesA valuesB = function
  | Map l -> 
      List.(for_all (fun (k, v) -> 
        mem k valuesA && mem v valuesB) l)

let handle_entry = function
  | Field (k, vs) -> Left (k, vs)
  | Message (_, field) ->
      match field with
      | Message_fields (_, v) -> Right v


let check_stmt map = function
| Fields es -> 
    List.fold_left (fun map x -> 
      let k, v = x |> handle_entry |> find_left |> get in
      Fields.add k v map) map es
| Messages es ->
    let fields =
      es |> List.map handle_entry 
         |> List.map find_right |> List.map get
         |> List.for_all (fun x -> Fields.mem x map) in
    if fields then map else 
      raise (SemanticError "Found a message associated to a non-declared field")
| Function (name, ty, body) -> 
    match ty with
    | Some (a, b) -> 
        (try
          let valuesA = Fields.find a map in
          let valuesB = Fields.find b map in

          if (check_func_body valuesA valuesB body) then map else
            raise (SemanticError (name ^ " function's values don't correspond to its type"))
        with
          Not_found -> raise (SemanticError ("Function " ^ name ^ " has undefined type")))
    | None -> map

let check_program = function
  (fields, messages, functions) as ast ->
    let fields = value fields ~default: (Fields []) in
    let messages = value messages ~default: (Messages []) in
    let _ = List.fold_left check_stmt Fields.empty (fields :: messages :: functions) in
    ast
