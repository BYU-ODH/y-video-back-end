(defproject y-video-back "0.1.0-SNAPSHOT"

  :description "Default template for a BYU ODH Clojure Program"
  :url "http://example.com/FIXME"

  :dependencies [[byu-odh/byu-cas "1"]
                 [camel-snake-kebab "0.4.1"]
                 [cheshire "5.10.0"]
                 [clj-http "3.10.0"]
                 [com.mchange/c3p0 "0.9.5.5"]
                 [cprop "0.1.16"]
                 [garden "1.3.9"]
                 [hiccup "1.0.5"]
                 [hikari-cp "2.11.0"]
                 [honeysql "0.9.10"]
                 [luminus-immutant "0.2.5"]
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
                 [ring/ring-defaults "0.3.2"]
                 [tick "0.4.20-alpha"]]

  :min-lein-version "2.0.0"
  :eastwood {:linters [:all]
             :exclude-linters [:unused-private-vars :unused-fn-args :duplicate-params :unused-locals :keyword-typos :unused-namespaces]
             :exclude-namespaces [:test-paths]}
  :test-paths ["test/clj/unit" "test/clj/usecase"]
  :jvm-opts ["-server" "-Dconf=.lein-env"]
  :source-paths ["src/clj" "src/cljc" "src/cljs"]
  :resource-paths ["resources"]
  :target-path "target/"
  :main y-video-back.core
  :plugins [[lein-cprop "1.0.3"]
            [migratus-lein "0.7.2"]
            [lein-immutant "2.1.0"]
            [lein-garden "0.3.0"]]
  :garden {:builds [{:id "y-video-back-viz"
                     :source-path "src/clj/y-video-back/styles"
                     :stylesheet y-video-back.styles.style/y-video-back
                     :compiler {:output-to "resources/public/css/style.css"
                                :pretty-print? true}}]}
  :clean-targets ^{:protect false}
  [:target-path [:builds :app :compiler :output-dir] [:builds :app :compiler :output-to]]

  :immutant {:war {:context-path "/"
                   :name "y-video-back%t"}}
  :test-selectors {:default (complement :integration)
                 :integration :integration}

  :profiles
  {:uberjar {:omit-source true
             :aot :all
             :uberjar-name "y-video-back.jar"
             :source-paths ["env/prod/clj"]
             :resource-paths ["env/prod/resources"]}

   :dev           [:project/dev :profiles/dev]
   :test          [:project/dev :project/test :profiles/test]


   :project/dev  {:dependencies [ ;[garden-gnome "0.1.0"]
                                 [pjstadig/humane-test-output "0.10.0"]
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
   :profiles/test {}})
