(ns challenge-autorizador.logic.account-test
  (:require [clojure.test :refer :all]
            [challenge-autorizador.logic.account :refer :all]))

(deftest inactive-card?-test
  (testing "Que o cartão está inativo"
    (is (inactive-card? {:active-card false, :available-limit 1000})))

  (testing "Que o cartão não está ativo"
   (is (not (inactive-card? {:active-card true, :available-limit 1000})))))

(deftest invalid-account?-test
  (testing "Que a conta não foi criada"
    (is (invalid-account? {})))

  (testing "Que a conta foi criada"
   (is (not (invalid-account? {:active-card true, :available-limit 1000})))))