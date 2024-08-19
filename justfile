release:
    mvn -U -Darguments="-DskipTests -Prelease" release:prepare release:perform --batch-mode
    git push
    git checkout main
    git fetch --prune
    git pull
    git branch -D release
