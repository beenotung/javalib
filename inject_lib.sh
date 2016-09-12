#!/bin/bash
#
#   Example : ./inject_lib.sh
#       install / update library
#
#   Example : ./inject_lib.sh --delete
#       uninstall library
#

## const
tmp="/tmp/inject_lib";
lib_url="https://github.com/beenotung/javalib.git";
lib_class="Utils";
lib_root="$tmp/javalib"
lib_file="$lib_root/src/github.com/beenotung/javalib/$lib_class.java"
key="_inject_lib_"

## param ## TODO use arguments
main_class="Main";
main_file="src/$main_class.java";

if [ ! -f "$main_file" ]; then
  echo "Error : main file \"$main_file\" not found!";
  exit 1;
fi

## check if already injected
res=$(grep "$key" "$main_file" | wc -l)
if [ "$res" != "0" ]; then
  echo "existing library detected, deleting current version...";
  ### delete lib dep
  sed -i '/\/\* lib dep begin \*\//,/\/\* lib dep end \*\//d' "$main_file";
  ### delete lib code
  sed -i '/\/\* lib code begin \*\//,/\/\* lib code end \*\//d' "$main_file";
  ### delete lib marker
  sed -i "/$key/d" "$main_file";
fi

## check if need to terminate
if [ "$1" == "--delete" ]; then
  exit 0;
fi

echo "injecting library...";
## clean folder
if [ -d "$lib_root" ]; then
  rm -rf "$lib_root";
fi
mkdir -p "$lib_root";
## download lib
#git clone "$lib_url" "$lib_root"; ## tmp disable
mkdir -p "$tmp";
cp "$lib_file" "$tmp/l";

## insert marker to indicate lib existence
sed -i "1 i /*$key*/" "$main_file"; ## tmp disable
#sed -i "1 i /*something*/" "$main_file"; ## tmp enable

## inject lib dependent
echo "/* lib dep begin */" > "$tmp/dep";
grep "^import " "$tmp/l" >> "$tmp/dep";
echo "/* lib dep end */" >> "$tmp/dep";
res=$(grep "^package " "$main_file" | wc -l)
if [ "$res" == "0" ]; then
#  echo "without package";
  sed -i "1 r $tmp/dep" "$main_file";
else
#  echo "with package";
  res=$(grep "^package " "$main_file" | wc -l);
  if [ "$res" != "1" ]; then
    echo "ERROR : unexpected format, cannot uniquely allocate package statement (^package )";
    exit 1;
  fi
  sed -i "/^package /r $tmp/dep" "$main_file";
fi
rm "$tmp/dep"

## inject lib code
cat "$tmp/l" | grep -v "^import " | grep -v "^package " > "$tmp/code";
### remove debug comment
sed -i "/^\/\//d" "$tmp/code";
### remove outer class and place marker
sed -i "s/^public class $lib_class {/\/* lib code begin *\//" "$tmp/code";
#sed -i '/^}/,/d' "$tmp/code";
sed -i "s/^}/\/* lib code end *\//" "$tmp/code";
### rename outer class into main class
sed -i "s/$lib_class/$main_class/g" "$tmp/code";
### crop leading/tailing blank lines
sed -i '1 d' "$tmp/code";
### copy code into main file
sed -i "/public class $main_class/r $tmp/code" "$main_file";

exit 0;


# deprecated old version of script, inject as external class, syntactically heavy

res=$(grep "class $lib_class" "$main_file" | wc -l)
if [ "$res" == "0" ]; then
  echo "injecting $lib_class..."
  if [ -d "$lib_root" ]; then
    rm -rf "$lib_root";
  fi
  git clone "$lib_url" "$lib_root";
  cat "$lib_file" | grep -v "package" | sed "s/public class $lib_class/class $lib_class/" > "$tmp/$lib_class"
  cp "$main_file" "$tmp/main"
  sed -i '/public class/i \
_inject_lib_\
' "$main_file";
  sed -i "/_inject_lib_/r $tmp/$lib_class" "$main_file";
  sed -i '/_inject_lib_/d' "$main_file"
  rm -rf "$tmp"
else
  echo "$lib_class already exist, skip inject"
fi
