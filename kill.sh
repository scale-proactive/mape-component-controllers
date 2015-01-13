echo -n "Cleaning trauco...   "
ssh trauco "killall java"
echo -n "Cleaning titan...    "
ssh titan "killall java"
echo -n "Cleaning caleuche... "
ssh caleuche "killall java"
echo -n "Cleaning tripio...   "
ssh tripio "killall java"

