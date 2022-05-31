## service_handlers

These files contain most of the logic for the api handlers. In this readme, we'll discuss what a handler looks like, as well as how to add new routes/endpoints.

We'll use the `GET /api/user/{id}` endpoint as an example.

```clojure
(def user-get-by-id
  {:summary "Retrieves specified user"
   :parameters {:header {:session-id uuid?}
                :path {:id uuid?}}
   :responses {200 {:body models/user}
               404 {:body {:message string?}}}
   :handler (fn [{{{:keys [session-id]} :header {:keys [id]} :path} :parameters}]
              (if-not (ru/has-permission session-id "user-get-by-id" 0)
                ru/forbidden-page
                (let [user-result (users/READ id)]
                  (if (nil? user-result)
                    {:status 404
                     :body {:message "user not found"}
                     :headers {"session-id" session-id}}
                    {:status 200
                     :body user-result
                     :headers {"session-id" session-id}}))))})
```

`:summary` gives a brief description of the endpoint. These are displayed on the `/api/api-docs/` page.

`:parameters` describes what request parameters the endpoint requires. In this case, `GET /api/user/{id}` requires a UUID called `session-id` in the request header and a UUID in place of `{id}` in the request path. Many endpoints also require specific fields in the request body. If any of these fields is the wrong type (string, int, boolean, UUID, etc), the api will throw a 400 error and abort the request. With the exception of PATCH routes, any request missing a required field will also be aborted with status 400.

`:responses` describes what the responses to this endpoint can look like. If a field in the response is missing or ill-typed, the api will throw a 500 error. However, this only happens after the handler function has been called. So, even if a response coercion error is thrown, the database has likely already been committed with any changes.

`:handler` gives the handler function. This function is called with request as its argument. This function must return one of the response objects described in `:responses`.

### permissions

All handlers begin be checking the request's permissions. They do this by calling `role-utils/has-permission`. Eventually, this function will take the session-id and endpoint name and then lookup the permission required in a large switch table. It will then return true if the request has permission to continue. Currently, however, this function returns true if the session-id is either any valid user id or the `session-id-bypass` from the `config.edn` file.

### session-id

Almost all responses include a `session-id` in their header. All session-id's have a determined life. If a session id is not used for a certain amount of time the session-id will expire. If the session-id is in used it will push back it's expiration time.

## adding new endpoints

New endpoints must be added in 3 places:

1. `../services.clj`
2. `handlers.clj`
3. `{table-name}_handlers.clj`

In each location, base the new endpoint components on existing ones.

For more information, see the reitit documentation: [https://metosin.github.io/reitit/](https://metosin.github.io/reitit/)
