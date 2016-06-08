$doc = document;
$wnd = window;

$stats = function() {
};
$self = self;
$sessionId = null;

function __MODULE_FUNC__() {
  // TODO(zundel): Add slot for property providers.
  var strongName;
  var softPermutationId = 0;
  try {
    // __PERMUTATIONS_BEGIN__
    // Permutation logic
    // __PERMUTATIONS_END__
  } catch (e) {
    var errorMsg = {
      "worker bootstrap error" : e.message
    };
    self.postMessage(JSON.stringify(errorMsg));
    return;
  }
  var idx = strongName.indexOf(':');
  if (idx != -1) {
    softPermutationId = Number(strongName.substring(idx + 1));
    strongName = strongName.substring(0, idx);
  }
  loadScript("__MODULE_NAME__/" + strongName + ".cache.js", function() {
            gwtOnLoad(undefined, '__MODULE_NAME__', '', softPermutationId);
        }
    );
}

function loadScript(path, callback) {
    var script = document.createElement('script');
    script.type = 'text/javascript';
    script.src = path;
    script.onload = function() {
        if(typeof(callback) === 'function') callback();
    };
    script.onerror = function() {
        throw('Error loading ' + path);
    };
    document.getElementsByTagName('head')[0].appendChild(script);
}

__MODULE_FUNC__();
