type field_t =
  | Single
  | Set

type statement =
  | Fields of entry list
  | Messages of entry list
  | Function of string * (string * string) option * func_body

and entry = 
  | Field of string * string list
  | Message of string * value 

and value =
  Message_fields of field_t * string (* make this string list in the long term *)

and func_body =
  | Map of (string * string) list

  

let handle_ty ty =
  match ty with
  | Single -> "Single"
  | Set  -> "Set"

let handle_message ast =
  match ast with
  | Message (mes, v) ->
      (match v with
      | Message_fields (t, field) -> (mes, handle_ty t, field))
  | _ -> invalid_arg "Expected message"

let handle_field ast =
  match ast with
  | Field (k, vs) -> (k, vs)
  | _ -> invalid_arg "Expected field"

let get_body body =
  match body with
  | Map l -> l

let handle_function f =
  match f with
  | Function (name, _, body) -> (name, get_body body)
  | _ -> invalid_arg "Expected function"


let get_fields ast =
  match ast with
  | (fields, _, _) ->
    Option.map (fun x -> 
      match x with 
      | Fields es -> es
      | _ -> invalid_arg "Expected fields") fields
    |> Option.value ~default:[]
    |> List.map handle_field

let get_messages_map ast =
  match ast with
  | (_, messages, _) ->
    Option.map (fun x -> 
      match x with 
      | Messages es -> es
      | _ -> invalid_arg "Expected messages") messages
    |> Option.value ~default:[]
    |> List.map handle_message 

let get_functions ast =
  match ast with
  | (_, _, functions) -> 
    List.map handle_function functions
