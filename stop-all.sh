for d in */ ; do
    echo "$d"
    cd $d
    ./stop.sh
    cd ..
done

