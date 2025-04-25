module StringSet = Set.Make(String)
module StringMap = Map.Make(String)

type field_opt =
  | List
  | Set

type field_name = string
type message_type = string

type statement =
  | Fields of entry list
  | Messages of entry list
  | Function of string * func_type * func_body

and entry = 
  | Field of field_name * string list
  | Message of (message_type * value)

and value =
  Message_val of field_opt * field_name

and func_type =
  | Mapping

and func_body =
  | Map of (string * string) list


let convert_opt opt =
  match opt with
  | List -> "List"
  | Set  -> "Set"

let handle_message ast =
  match ast with
  | Message (mes, v) ->
      (match v with
      | Message_val (opt, field) -> (mes, opt, field))
  | _ -> invalid_arg "Expected message"

let handle_field ast =
  match ast with
  | Field (k, vs) ->
      (k, vs)
  | _ -> invalid_arg "Expected field"
let get_ty ty =
  match ty with
  | Mapping -> "Map"

let get_body body =
  match body with
  | Map l -> l

let rec handle_functions fs =
  match fs with
  | x :: xs ->
      begin
      match x with
      | Function (name, ty, body) ->
          let ty_str = get_ty ty in
          let body = get_body body in
          (name, ty_str, body) :: handle_functions xs
      | _ -> invalid_arg "Expected function"
      end
  | [] -> []

let get_fields ast =
  match ast with
  | (fields, _, _) ->
    match fields with
    | Some (Fields es) -> 
        List.fold_left (fun map x -> 
          let k, v = handle_field x in
          StringMap.add k v map) StringMap.empty es
        |> StringMap.to_list
    | None -> StringMap.(empty |> to_list)
  | _ -> assert false

let get_messages_map ast =
  match ast with
  | (_, messages, _) ->
    match messages with
    | Some (Messages es) ->
        List.fold_left (fun map x -> 
          let k, opt, v = handle_message x in
          let opt_n = convert_opt opt in
          StringMap.add k (opt_n, v) map) StringMap.empty es
        |> StringMap.to_list
    | None -> StringMap.(empty |> to_list)
  | _ -> assert false

let get_functions ast =
  match ast with
  | (_, _, functions) -> handle_functions functions