(ns user)

(when-let [start-nrepl! (requiring-resolve 'jarrett.common-nrepl/start-server!)]
  (start-nrepl!))