# Check front end tests
echo "-- Running front end tests --"
cd yvideo-client
npm test -- --watchAll=false

# If tests failed, exit script
if [ $? -ne 0 ]
then
cd ../
echo "-- Front end test(s) failed --"
exit 1
fi

# Delete old build
echo "-- Removing old build --"
rm -rf build

# Build new front end
echo "-- Building new front end --"
NODE_ENV=dev npm run build --dev --configuration=dev

# Copy new built files into back end resources
echo "-- Copying new build into back end resources --"
cd ../

rm resources/html/index.html
rm resources/public/_redirects
rm resources/public/asset-manifest.json
rm resources/public/favicon.ico
rm resources/public/manifest.json
rm resources/public/precache*.js
rm resources/public/service-worker.js
rm -rf resources/public/static

cp yvideo-client/build/index.html resources/html/
cp yvideo-client/build/_redirects resources/public/
cp yvideo-client/build/asset-manifest.json resources/public/
cp yvideo-client/build/favicon.ico resources/public/
cp yvideo-client/build/manifest.json resources/public/
cp yvideo-client/build/precache*.js resources/public/
cp yvideo-client/build/service-worker.js resources/public/
cp -r yvideo-client/build/static resources/public/

echo "-- Done --"
