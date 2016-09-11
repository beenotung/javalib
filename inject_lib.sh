#!/bin/bash
## const
tmp="/tmp/inject_lib";
lib_url="https://github.com/beenotung/javalib.git";
lib_class="Utils";
lib_root="$tmp/javalib"
lib_file="$lib_root/src/github.com/beenotung/javalib/$lib_class.java"

## param ## TODO use arguments
main_file="src/Main.java";

if [ ! -f "$main_file" ]; then
  echo "Error : main file \"$main_file\" not found!";
  exit 1;
fi

res=$(grep "class $lib_class" "$main_file" | wc -l)
if [ "$res" == "0" ]; then
  echo "injecting $lib_class..."
  if [ -d "$lib_root" ]; then
    rm -rf "$lib_root";
  fi
  git clone "$lib_url" "$lib_root";
  cat "$lib_file" | grep -v "package" | sed "s/public class $lib_class/class $lib_class/" > "$tmp/$lib_class"
  cp "$main_file" "$tmp/main"
  code=$(cat "$tmp/$lib_class")
#  sed -i "/public class/i \
#$code
#  " "$main_file"
  sed -i '/public class/i \
_inject_lib_' "$main_file";
  sed -i "/_inject_lib_/r $tmp/$lib_class" "$main_file";
  sed -i '/_inject_lib_/d' "$main_file"
#  echo "$code"
#  sed -i "/public class/i $tmp/$lib_class" "$main_file"
#  cat "$tmp/$lib_class" "$tmp/main" > "$main_file"
else
  echo "$lib_class already exist, skip inject"
fi
