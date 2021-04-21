(defproject y-video-back "0.1.0-SNAPSHOT"

  :description "Default template for a BYU ODH Clojure Program"
  :url "http://example.com/FIXME"

  :dependencies [[byu-odh/byu-cas "5"]
                 [camel-snake-kebab "0.4.1"]
                 [cheshire "5.10.0"]
                 [clj-http "3.10.0"]
                 [com.draines/postal "2.0.3"]
                 [com.mchange/c3p0 "0.9.5.5"]
                 [cprop "0.1.16"]
                 [hiccup "1.0.5"]
                 [hikari-cp "2.11.0"]
                 [honeysql "0.9.10"]
                 [figwheel-sidecar "0.5.20"]
                 [luminus-immutant "0.2.5"]
                 [luminus-transit "0.1.2"]
                 [luminus/ring-ttl-session "0.3.3"]
                 [luminus-migrations "0.6.7"]
                 [metosin/compojure-api "1.1.13"]
                 [metosin/ring-http-response "0.9.1"]
                 [mount "0.1.16"]
                 [com.taoensso/timbre "4.10.0"]
                 [org.clojure/clojure "1.10.1"]
                 [org.clojure/tools.cli "1.0.194"]
                 [org.postgresql/postgresql "42.2.12"]
                 [metosin/reitit "0.4.2"]
                 [metosin/reitit-schema "0.4.2"]
                 [metosin/reitit-frontend "0.4.2"]
                 [ring-middleware-format "0.7.4"]
                 [ring-webjars "0.2.0"]
                 [ring/ring-core "1.8.0"]
                 [ring-partial-content "2.0.1"]
                 ;[clj-commons/clj-yaml "0.7.1"]
                 [ring/ring-defaults "0.3.2"]
                 [selmer "1.12.23"]
                 [tick "0.4.20-alpha"]
                 [metosin/ring-swagger "0.26.2"]
                 [ring-cors "0.1.9"]
                 [clj-http "3.10.1"]
                 [clojure.java-time "0.3.1"]
                 [org.clojure/data.json "1.0.0"]
                 [nilenso/honeysql-postgres "0.2.6"]]
                 ;[ring/ring-ssl "0.3.0"]]

  :min-lein-version "2.0.0"
  :eastwood {:linters [:all]
             :exclude-linters [:keyword-typos :unused-locals :redefd-vars]}
             ; :exclude-linters [:unused-private-vars :unused-fn-args :duplicate-params :unused-locals :keyword-typos :unused-namespaces]
             ; :exclude-namespaces [:test-paths]}
  :test-paths ["test/clj/unit" "test/clj/usecase"]
  :jvm-opts ["-server" "-Dconf=.lein-env"]
  :source-paths ["src/clj" "src/cljc" "src/cljs"]
  :resource-paths ["resources"]
  :target-path "target/"
  :main y-video-back.core
  :plugins [[lein-cprop "1.0.3"]
            [migratus-lein "0.7.2"]
            [lein-immutant "2.1.0"]
            [lein-cloverage "1.1.2"]
            [jonase/eastwood "0.4.0"]]
  :clean-targets ^{:protect false}
  [:target-path [:builds :app :compiler :output-dir] [:builds :app :compiler :output-to]]

  :immutant {:war {:context-path "/"
                   :name "y-video-back%t"}}
  :test-selectors {:integration :integration
                   :mock-prod :mock-prod}

  :profiles
  {:uberjar {:omit-source true
             :aot :all
             :uberjar-name "y-video-back-end.jar"
             :source-paths ["env/prod/clj"]
             :resource-paths ["env/prod/resources"]}

   :dev           [:project/dev :profiles/dev]
   :test          [:project/dev :project/test :profiles/test]


   :project/dev  {:dependencies [[pjstadig/humane-test-output "0.10.0"]
                                 [prone "2020-01-17"]
                                 [ring/ring-devel "1.8.0"]
                                 [ring/ring-mock "0.4.0"]]
                  :plugins      [[com.jakemccrary/lein-test-refresh "0.14.0"]]
                  :source-paths ["env/dev/clj" "test/clj/unit" "test/clj/usecase"]
                  :resource-paths ["env/dev/resources"]
                  :injections [(require 'pjstadig.humane-test-output)
                               (pjstadig.humane-test-output/activate!)]}
   :project/test {:resource-paths ["env/test/resources"]}
   :profiles/dev {:repl-options {:init-ns user}}
   :profiles/test {:cloverage {:ns-exclude-regex [#"y-video-back\.common|y-video-back\.core|y-video-back\.db\.migratus|y-video-back\.env|y-video-back\.figwheel|y-video-back\.handler|y-video-back\.layout|y-video-back\.middleware\.exception|y-video-back\.middleware\.formats|y-video-back\.nrepl|user|y-video-back.config|y-video-back.dev-middleware|.*tests.*|legacy.*|stories.*|y-video-back\.model-specs"]}}})
