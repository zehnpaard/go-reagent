(defproject go-reagent "0.0.1"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.293"]
                 [reagent "0.6.0"]]

  :plugins [[lein-cljsbuild "1.1.4"]
            [lein-figwheel "0.5.8"]]

  :cljsbuild
  {:builds
   {:dev {:source-paths ["src"]
          :figwheel true
          :compiler {:output-to "resources/public/js/main.js"
                     :output-dir "resources/public/js/out/"}}}})
