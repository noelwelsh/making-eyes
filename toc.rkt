#lang racket

(provide
 toc
 make-toc)

(define toc
  '("intro.html"
    "setup.html"
    "request-handlers.html"
    "running.html"
    "concurrency.html"
    "http-client.html"
    "testing.html"
    "json.html"
    "mongo.html"
    "streaming.html"
    "deployment.html"
    ))

(define toc-file-name "_includes/toc.html")

(define (make-toc toc)
  (define (build-toc toc)
    (displayln "<ul>")
    (map build-item toc)
    (displayln "</ul>"))
  (define (build-item item)
    (cond
     [(string? item)
      (printf "<li><a href=\"~a\">~a</a></li>\n" item (title item))]
     [(pair? item)
      (build-toc item)]))

  (with-output-to-file toc-file-name
    (lambda () (build-toc toc))
    #:exists 'replace))


(define (source-file-name file-name)
  (cond
   [(file-exists? file-name)
    file-name]
   [(file-exists? (regexp-replace ".html" file-name ".markdown"))
    (regexp-replace ".html" file-name ".markdown")]
   [else
    (error (format "~a does not exist" file-name))]))

(define (title file-name)
  (with-input-from-file (source-file-name file-name)
    (lambda ()
      (define (find-title)
        (let* ([line (read-line)]
               [_ (if (eof-object? line)
                      (error (format "No title found in ~a\n" file-name))
                      #t)]
               [mtch (regexp-match #rx"^title: (.*)$" line)])
          (match mtch
                 [(cons _ (list ttl)) ttl]
                 [#f (find-title)])))
      (find-title))))
