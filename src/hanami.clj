(ns hanami
  (:require
   [aerial.hanami.common :as hc]
   [aerial.hanami.templates :as ht]
   [nextjournal.clerk :as clerk]
   [tablecloth.api :as tc]))

(clerk/serve! {:watch-paths ["src"] :browse? true})
(clerk/show! "src/hanami.clj")

(clerk/set-viewers!
 [{:pred tc/dataset?
   :transform-fn #(clerk/table {:head (tc/column-names %)
                                :rows (tc/rows %)})}])

(def dataset
  (-> "/Users/kira/code/projects/hanami-workshop-2/data/temperature-anomaly.csv"
      (tc/dataset)
      (tc/rename-columns {"Entity" :entity
                          "Code" :code
                          "Year" :year
                          "Median temperature anomaly from 1961-1990 average" :median
                          "Upper bound (95% CI)" :upper-ci
                          "Lower bound (95% CI)" :lower-ci})
      (tc/select-rows #(= (:entity %) "Global"))
      (tc/convert-types {:year :string})
      (tc/select-columns [:year :median :upper-ci :lower-ci])))

(def error-bar-chart
  (hc/xform ht/layer-chart
            :DATA (-> dataset (tc/rows :as-maps))
            :WIDTH 700
            :HEIGHT 500
            :BACKGROUND "white"
            :LAYER [(hc/xform ht/line-layer :Y :median :X :year :XTYPE :temporal)
                    {:mark "errorband"
                     :encoding
                     {:y
                      {:field :upper-ci,
                       :type "quantitative"
                       :title "Median temperature anomaly from 1961-1990 average (95% CI)"},
                      :y2 {:field :lower-ci},
                      :x {:field :year, :type "temporal"}}}]))

;; ## Average temperature anomaly, Global
;; ### Global average land-sea temperature anomaly relative to the 1961-1990 average temperature.
;; Data from [Our World in Data](https://ourworldindata.org/grapher/temperature-anomaly?country=~Global)

(clerk/vl error-bar-chart)

(def birthweight-data
  (-> "/Users/kira/code/projects/hanami-workshop-2/data/birthwt.csv"
      (tc/dataset {:key-fn keyword})
      ))

(def cars
  {:$schema "https://vega.github.io/schema/vega-lite/v5.json",
   :data (-> "/Users/kira/code/projects/hanami-workshop-2/data/cars.csv"
             (tc/rows :as-maps)),
   :mark "bar",
   :encoding
   {:x {:bin {:maxbins 15}, :field "Horsepower", :type "quantitative"},
    :y {:aggregate "count", :type "quantitative"},
    :row {:field "Origin"}}})

(def histogram
  {:description "Birthweights",
   :data (-> birthweight-data (tc/rows :as-maps))
   :width {:step 17},
   :mark "bar",
   :encoding
   {;;:row {:field "gender"},
    :y {:aggregate "count", :title "count"},
    :x {:field :bwt
        :bin {:maxbins 15}},
    ;; :color ;; {:field "gender", :scale {:range ["#675193" "#ca8861"]}}
    }})

(clerk/vl histogram)
