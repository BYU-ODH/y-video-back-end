rm resources/html/index.html
rm resources/public/_redirects
rm resources/public/asset-manifest.json
rm resources/public/favicon.ico
rm resources/public/manifest.json
rm resources/public/precache*.js
rm resources/public/service-worker.js
rm -rf resources/public/static
rm -rf resources/public/videos

cd yvideo-client
rm -rf build

npm run build
#NODE_ENV=dev npm run build --dev --configuration=dev

cd ../

cp yvideo-client/build/index.html resources/html/
cp yvideo-client/build/_redirects resources/public/
cp yvideo-client/build/asset-manifest.json resources/public/
cp yvideo-client/build/favicon.ico resources/public/
cp yvideo-client/build/manifest.json resources/public/
cp yvideo-client/build/precache*.js resources/public/
cp yvideo-client/build/service-worker.js resources/public/
cp -r yvideo-client/build/static resources/public/
cp -r yvideo-client/build/videos resources/public/
