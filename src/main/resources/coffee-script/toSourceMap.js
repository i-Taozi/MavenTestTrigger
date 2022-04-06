try {
  var v3sourceMap = JSON.parse(CoffeeScript.compile(__source, {bare: true, literate: __literate, sourceMap: true, filename: __filename}).v3SourceMap);
  v3sourceMap.file = __filename;
  v3sourceMap.sources = [__sourcename];
  delete v3sourceMap['sourceRoot'];
  JSON.stringify(v3sourceMap, null, " ");
} catch (exception) {
  throw 'Unable to compile CoffeeScript ' + exception;
}