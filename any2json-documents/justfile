build: copy-pdfs
    mkdocs build
    
run: copy-pdfs
    mkdocs serve

deploy: copy-pdfs
    mkdocs gh-deploy

@copy-pdfs:
    cp whitepapers/Semi-structured\ Document\ Feature\ Extraction/misc/main.pdf docs/resources/feature-extraction.pdf
    cp whitepapers/Table\ Layout\ Regular\ Expression\ -\ Layex/misc/main.pdf docs/resources/layex.pdf