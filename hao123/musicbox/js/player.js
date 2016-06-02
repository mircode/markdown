// @license
// Baidu Music Player: 0.9.2
// -------------------------
// (c) 2014 FE Team of Baidu Music
// Can be freely distributed under the BSD license.
// https://github.com/Baidu-Music-FE/muplayer/blob/master/dist/player.js
(function(t, e) {
    if (typeof t._mu === "undefined") {
        t._mu = {}
    }
    if (typeof exports === "object") {
        return module.exports = e()
    } else if (typeof define === "function" && define.amd) {
        return define("muplayer/core/cfg", e)
    } else {
        return t._mu.cfg = e()
    }
}
)(this, function() {
    var t;
    t = this;
    return $.extend({
        namespace: t._mu,
        debug: false,
        version: "0.9.2",
        timerResolution: 25,
        cdn: "http://apps.bdimg.com/libs/muplayer/",
        engine: {
            TYPES: {
                FLASH_MP3: "FlashMP3Core",
                FLASH_MP4: "FlashMP4Core",
                AUDIO: "AudioCore"
            },
            EVENTS: {
                STATECHANGE: "engine:statechange",
                POSITIONCHANGE: "engine:postionchange",
                PROGRESS: "engine:progress",
                ERROR: "engine:error",
                INIT: "engine:init",
                INIT_FAIL: "engine:init_fail",
                WAITING_TIMEOUT: "engine:waiting_timeout"
            },
            STATES: {
                CANPLAYTHROUGH: "canplaythrough",
                PREBUFFER: "waiting",
                BUFFERING: "loadeddata",
                PLAYING: "playing",
                PAUSE: "pause",
                STOP: "suspend",
                END: "ended"
            },
            ERRCODE: {
                MEDIA_ERR_ABORTED: "1",
                MEDIA_ERR_NETWORK: "2",
                MEDIA_ERR_DECODE: "3",
                MEDIA_ERR_SRC_NOT_SUPPORTED: "4"
            }
        }
    }, typeof t._mu === "undefined" ? {} : t._mu.cfg)
}
);
(function(t, e) {
    if (typeof exports === "object") {
        return module.exports = e()
    } else if (typeof define === "function" && define.amd) {
        return define("muplayer/core/utils", ["muplayer/core/cfg"], e)
    } else {
        return t._mu.utils = e(t._mu.cfg)
    }
}
)(this, function(t) {
    var e, n, r, i, o, s, u, a, f, p, l, c;
    f = {};
    i = String.prototype;
    n = Number.prototype;
    r = Object.prototype;
    e = Array.prototype;
    u = e.push;
    o = r.hasOwnProperty;
    a = r.toString;
    c = ["Arguments", "Function", "String", "Number", "Date", "RegExp"];
    for (p = 0,
    l = c.length; p < l; p++) {
        s = c[p];
        f["is" + s] = function(t) {
            return function(e) {
                return a.call(e) === "[object " + t + "]"
            }
        }
        (s)
    }
    if (!$.isFunction(i.startsWith)) {
        i.startsWith = function(t) {
            return this.slice(0, t.length) === t
        }
    }
    if (!$.isFunction(i.endsWith)) {
        i.endsWith = function(t) {
            return this.slice(-t.length) === t
        }
    }
    n.toFixed = function(t) {
        var e, n, r;
        r = Math.pow(10, t);
        e = (Math.round(this * r) / r).toString();
        if (t === 0) {
            return e
        }
        if (e.indexOf(".") < 0) {
            e += "."
        }
        n = t + 1 - (e.length - e.indexOf("."));
        while (n--) {
            e += "0"
        }
        return e
    }
    ;
    $.extend(f, {
        isBoolean: function(t) {
            return t === true || t === false || a.call(t) === "[object Boolean]"
        },
        has: function(t, e) {
            return o.call(t, e)
        },
        random: function(t, e) {
            if (!e) {
                e = t;
                t = 0
            }
            return t + Math.floor(Math.random() * (e - t + 1))
        },
        shuffle: function(t) {
            var e, n, r, i, o, s;
            e = 0;
            i = [];
            for (o = 0,
            s = t.length; o < s; o++) {
                n = t[o];
                r = this.random(e++);
                i[e - 1] = i[r];
                i[r] = n
            }
            return i
        },
        time2str: function(t) {
            var e, n, r, i, o, s;
            o = [];
            e = Math.floor;
            t = Math.round(t);
            n = e(t / 3600);
            r = e((t - 3600 * n) / 60);
            s = t % 60;
            i = function(t) {
                return function(t, e) {
                    var n, r, i;
                    r = "";
                    n = "";
                    if (t < 0) {
                        n = "-"
                    }
                    i = String(Math.abs(t));
                    if (i.length < e) {
                        r = new Array(e - i.length + 1).join("0")
                    }
                    return n + r + i
                }
            }
            (this);
            if (n) {
                o.push(n)
            }
            o.push(i(r, 2));
            o.push(i(s, 2));
            return o.join(":")
        },
        namespace: function() {
            var e, n, r, i, o, s, u, a, f, p;
            e = arguments;
            u = ".";
            for (a = 0,
            f = e.length; a < f; a++) {
                n = e[a];
                s = t.namespace;
                if (n.indexOf(u) > -1) {
                    r = n.split(u);
                    p = [0, r.length],
                    i = p[0],
                    o = p[1];
                    while (i < o) {
                        s[r[i]] = s[r[i]] || {};
                        s = s[r[i]];
                        i++
                    }
                } else {
                    s[n] = s[n] || {};
                    s = s[n]
                }
            }
            return s
        },
        wrap: function(t, e) {
            return function() {
                var n;
                n = [t];
                u.apply(n, arguments);
                return e.apply(this, n)
            }
        },
        toAbsoluteUrl: function(t) {
            var e;
            e = document.createElement("div");
            e.innerHTML = "<a></a>";
            e.firstChild.href = t;
            e.innerHTML = e.innerHTML;
            return e.firstChild.href
        }
    });
    return f
}
);
(function(t, e) {
    if (typeof exports === "object") {
        module.exports = e()
    } else if (typeof define === "function" && define.amd) {
        define("muplayer/lib/Timer", e)
    } else {
        t._mu.Timer = e()
    }
}
)(this, function() {
    function t(t) {
        if (typeof t === "string") {
            if (isNaN(parseInt(t, 10))) {
                t = "1" + t
            }
            var e = t.replace(/[^a-z0-9\.]/g, "").match(/(?:(\d+(?:\.\d+)?)(?:days?|d))?(?:(\d+(?:\.\d+)?)(?:hours?|hrs?|h))?(?:(\d+(?:\.\d+)?)(?:minutes?|mins?|m\b))?(?:(\d+(?:\.\d+)?)(?:seconds?|secs?|s))?(?:(\d+(?:\.\d+)?)(?:milliseconds?|ms))?/);
            if (e[0]) {
                return parseFloat(e[1] || 0) * 864e5 + parseFloat(e[2] || 0) * 36e5 + parseFloat(e[3] || 0) * 6e4 + parseFloat(e[4] || 0) * 1e3 + parseInt(e[5] || 0, 10)
            }
            if (!isNaN(parseInt(t, 10))) {
                return parseInt(t, 10)
            }
        }
        if (typeof t === "number") {
            return t
        }
        return 0
    }
    function e(t, e) {
        return parseInt(t / e, 10) || 1
    }
    function n(e) {
        if (this instanceof n === false) {
            return new n(e)
        }
        this._notifications = [];
        this._resolution = t(e) || 1e3;
        this._running = false;
        this._ticks = 0;
        this._timer = null ;
        this._drift = 0
    }
    n.prototype = {
        start: function() {
            var t = this;
            if (!this._running) {
                this._running = !this._running;
                setTimeout(function e() {
                    t._ticks++;
                    for (var n = 0, r = t._notifications.length; n < r; n++) {
                        if (t._notifications[n] && t._ticks % t._notifications[n].ticks === 0) {
                            t._notifications[n].callback.call(t._notifications[n], {
                                ticks: t._ticks,
                                resolution: t._resolution
                            })
                        }
                    }
                    if (t._running) {
                        clearTimeout(t._timer);
                        t._timer = setTimeout(e, t._resolution + t._drift);
                        t._drift = 0
                    }
                }
                , this._resolution)
            }
            return this
        },
        stop: function() {
            if (this._running) {
                this._running = !this._running;
                clearTimeout(this._timer)
            }
            return this
        },
        reset: function() {
            this.stop();
            this._ticks = 0;
            return this
        },
        clear: function() {
            this.reset();
            this._notifications = [];
            return this
        },
        ticks: function() {
            return this._ticks
        },
        resolution: function() {
            return this._resolution
        },
        running: function() {
            return this._running
        },
        bind: function(n, r) {
            if (n && r) {
                var i = e(t(n), this._resolution);
                this._notifications.push({
                    ticks: i,
                    callback: r
                })
            }
            return this
        },
        unbind: function(t) {
            if (!t) {
                this._notifications = []
            } else {
                for (var e = 0, n = this._notifications.length; e < n; e++) {
                    if (this._notifications[e] && this._notifications[e].callback === t) {
                        this._notifications.splice(e, 1)
                    }
                }
            }
            return this
        },
        drift: function(t) {
            this._drift = t;
            return this
        }
    };
    n.prototype.every = n.prototype.bind;
    n.prototype.after = function(t, e) {
        var r = this;
        n.prototype.bind.call(r, t, function i() {
            n.prototype.unbind.call(r, i);
            e.apply(this, arguments)
        }
        );
        return this
    }
    ;
    return n
}
);
(function(t, e) {
    if (typeof exports === "object") {
        module.exports = e()
    } else if (typeof define === "function" && define.amd) {
        define("muplayer/lib/events", e)
    } else {
        t._mu.Events = e()
    }
}
)(this, function() {
    var t = /\s+/;
    function e() {}
    e.prototype.on = function(e, n, r) {
        var i, o, s;
        if (!n)
            return this;
        i = this.__events || (this.__events = {});
        e = e.split(t);
        while (o = e.shift()) {
            s = i[o] || (i[o] = []);
            s.push(n, r)
        }
        return this
    }
    ;
    e.prototype.once = function(t, e, n) {
        var r = this;
        var i = function() {
            r.off(t, i);
            e.apply(this, arguments)
        }
        ;
        this.on(t, i, n)
    }
    ;
    e.prototype.off = function(e, r, i) {
        var o, s, u, a;
        if (!(o = this.__events))
            return this;
        if (!(e || r || i)) {
            delete this.__events;
            return this
        }
        e = e ? e.split(t) : n(o);
        while (s = e.shift()) {
            u = o[s];
            if (!u)
                continue;if (!(r || i)) {
                delete o[s];
                continue
            }
            for (a = u.length - 2; a >= 0; a -= 2) {
                if (!(r && u[a] !== r || i && u[a + 1] !== i)) {
                    u.splice(a, 2)
                }
            }
        }
        return this
    }
    ;
    e.prototype.trigger = function(e) {
        var n, i, o, s, u, a, f = [], p, l = true;
        if (!(n = this.__events))
            return this;
        e = e.split(t);
        for (u = 1,
        a = arguments.length; u < a; u++) {
            f[u - 1] = arguments[u]
        }
        while (i = e.shift()) {
            if (o = n.all)
                o = o.slice();
            if (s = n[i])
                s = s.slice();
            l = r(s, f, this) && l;
            l = r(o, [i].concat(f), this) && l
        }
        return l
    }
    ;
    e.prototype.emit = e.prototype.trigger;
    e.mixTo = function(t) {
        t = i(t) ? t.prototype : t;
        var n = e.prototype;
        for (var r in n) {
            if (n.hasOwnProperty(r)) {
                t[r] = n[r]
            }
        }
    }
    ;
    var n = Object.keys;
    if (!n) {
        n = function(t) {
            var e = [];
            for (var n in t) {
                if (t.hasOwnProperty(n)) {
                    e.push(n)
                }
            }
            return e
        }
    }
    function r(t, e, n) {
        if (t) {
            var r = 0
              , i = t.length
              , o = e[0]
              , s = e[1]
              , u = e[2]
              , a = true;
            switch (e.length) {
            case 0:
                for (; r < i; r += 2) {
                    a = t[r].call(t[r + 1] || n) !== false && a
                }
                break;
            case 1:
                for (; r < i; r += 2) {
                    a = t[r].call(t[r + 1] || n, o) !== false && a
                }
                break;
            case 2:
                for (; r < i; r += 2) {
                    a = t[r].call(t[r + 1] || n, o, s) !== false && a
                }
                break;
            case 3:
                for (; r < i; r += 2) {
                    a = t[r].call(t[r + 1] || n, o, s, u) !== false && a
                }
                break;
            default:
                for (; r < i; r += 2) {
                    a = t[r].apply(t[r + 1] || n, e) !== false && a
                }
                break
            }
        }
        return a
    }
    function i(t) {
        return Object.prototype.toString.call(t) === "[object Function]"
    }
    return e
}
);
var __bind = function(t, e) {
    return function() {
        return t.apply(e, arguments)
    }
}
  , __indexOf = [].indexOf || function(t) {
    for (var e = 0, n = this.length; e < n; e++) {
        if (e in this && this[e] === t)
            return e
    }
    return -1
}
;
(function(t, e) {
    if (typeof exports === "object") {
        return module.exports = e()
    } else if (typeof define === "function" && define.amd) {
        return define("muplayer/core/playlist", ["muplayer/core/utils", "muplayer/lib/events"], e)
    } else {
        return t._mu.Playlist = e(_mu.utils, _mu.Events)
    }
}
)(this, function(t, e) {
    var n;
    n = function() {
        function e(t) {
            this.next = __bind(this.next, this);
            this.prev = __bind(this.prev, this);
            this.opts = $.extend({}, this.defaults, t);
            this.reset()
        }
        e.prototype.reset = function() {
            this.cur = "";
            if ($.isArray(this.list)) {
                return this.list.length = 0
            } else {
                return this.list = []
            }
        }
        ;
        e.prototype._resetListRandom = function(e) {
            var n, r, i;
            if (this.mode === "list-random") {
                e = e || 0;
                this._listRandomIndex = e;
                this._listRandom = t.shuffle(function() {
                    i = [];
                    for (var t = 0, e = this.list.length; 0 <= e ? t < e : t > e; 0 <= e ? t++ : t--) {
                        i.push(t)
                    }
                    return i
                }
                .apply(this));
                return this.cur = this.list[this._listRandom[e]]
            }
        }
        ;
        e.prototype._formatSid = function(e) {
            var n, r, i;
            n = this.opts.absoluteUrl;
            r = function(e) {
                return n && t.toAbsoluteUrl(e) || "" + e
            }
            ;
            return $.isArray(e) && function() {
                var t, n, o;
                o = [];
                for (t = 0,
                n = e.length; t < n; t++) {
                    i = e[t];
                    if (i) {
                        o.push(r(i))
                    }
                }
                return o
            }
            () || r(e)
        }
        ;
        e.prototype.setMode = function(t) {
            if (t === "single" || t === "random" || t === "list-random" || t === "list" || t === "loop") {
                this.mode = t
            }
            return this._resetListRandom()
        }
        ;
        e.prototype.add = function(t) {
            t = this._formatSid(t);
            this.remove(t);
            if ($.isArray(t) && t.length) {
                this.list = t.concat(this.list)
            } else if (t) {
                this.list.unshift(t)
            }
            this.trigger("playlist:add", t);
            return this._resetListRandom()
        }
        ;
        e.prototype.remove = function(t) {
            var e, n, r, i;
            n = function(t) {
                return function(e) {
                    var n;
                    n = $.inArray(e, t.list);
                    if (n !== -1) {
                        return t.list.splice(n, 1)
                    }
                }
            }
            (this);
            t = this._formatSid(t);
            if ($.isArray(t)) {
                for (r = 0,
                i = t.length; r < i; r++) {
                    e = t[r];
                    n(e)
                }
            } else {
                n(t)
            }
            this.trigger("playlist:remove", t);
            return this._resetListRandom()
        }
        ;
        e.prototype.prev = function() {
            var e, n, r, i;
            r = this.list;
            e = $.inArray(this.cur, r);
            n = r.length;
            i = e - 1;
            switch (this.mode) {
            case "single":
                i = e;
                break;
            case "random":
                i = t.random(0, n - 1);
                break;
            case "list":
                if (e = 0) {
                    this.cur = "";
                    return false
                }
                break;
            case "list-random":
                e = this._listRandomIndex--;
                i = e - 1;
                if (e === 0) {
                    i = n - 1;
                    this._resetListRandom(i)
                }
                return this.cur = r[this._listRandom[i]];
            case "loop":
                if (e === 0) {
                    i = n - 1
                }
            }
            return this.cur = r[i]
        }
        ;
        e.prototype.next = function() {
            var e, n, r, i;
            r = this.list;
            e = $.inArray(this.cur, r);
            n = r.length;
            i = e + 1;
            switch (this.mode) {
            case "single":
                i = e;
                break;
            case "random":
                i = t.random(0, n - 1);
                break;
            case "list":
                if (e === n - 1) {
                    this.cur = "";
                    return false
                }
                break;
            case "list-random":
                e = this._listRandomIndex++;
                i = e + 1;
                if (e === n - 1) {
                    i = 0;
                    this._resetListRandom(i)
                }
                return this.cur = r[this._listRandom[i]];
            case "loop":
                if (e === n - 1) {
                    i = 0
                }
            }
            return this.cur = r[i]
        }
        ;
        e.prototype.setCur = function(t) {
            t = this._formatSid(t);
            if (__indexOf.call(this.list, t) < 0) {
                this.add(t)
            }
            return this.cur = t
        }
        ;
        return e
    }
    ();
    e.mixTo(n);
    return n
}
);
var __indexOf = [].indexOf || function(t) {
    for (var e = 0, n = this.length; e < n; e++) {
        if (e in this && this[e] === t)
            return e
    }
    return -1
}
;
(function(t, e) {
    if (typeof exports === "object") {
        return module.exports = e()
    } else if (typeof define === "function" && define.amd) {
        return define("muplayer/core/engines/engineCore", ["muplayer/core/cfg", "muplayer/core/utils", "muplayer/lib/events"], e)
    } else {
        return t._mu.EngineCore = e(_mu.cfg, _mu.utils, _mu.Events)
    }
}
)(this, function(t, e, n) {
    var r, i, o, s, u, a, f;
    f = t.engine,
    r = f.EVENTS,
    o = f.STATES;
    s = function() {
        var t;
        t = [];
        for (u in o) {
            a = o[u];
            t.push(a)
        }
        return t
    }
    ();
    i = function() {
        function t() {}
        t.prototype._supportedTypes = [];
        t.prototype.getSupportedTypes = function() {
            return this._supportedTypes
        }
        ;
        t.prototype.canPlayType = function(t) {
            return $.inArray(t, this.getSupportedTypes()) !== -1
        }
        ;
        t.prototype.reset = function() {
            this.stop();
            this.setUrl();
            this.trigger(r.PROGRESS, 0);
            return this.trigger(r.POSITIONCHANGE, 0)
        }
        ;
        t.prototype.play = function() {
            return this
        }
        ;
        t.prototype.pause = function() {
            return this
        }
        ;
        t.prototype.stop = function() {
            return this
        }
        ;
        t.prototype.setUrl = function(t) {
            if (t == null ) {
                t = ""
            }
            this._url = t;
            return this
        }
        ;
        t.prototype.getUrl = function() {
            return this._url
        }
        ;
        t.prototype.setState = function(t) {
            var e, n;
            if (__indexOf.call(s, t) < 0 || t === this._state) {
                return
            }
            if (t === o.BUFFERING && ((n = this._state) === o.END || n === o.PAUSE || n === o.STOP)) {
                return
            }
            e = this._state;
            this._state = t;
            return this.trigger(r.STATECHANGE, {
                oldState: e,
                newState: t
            })
        }
        ;
        t.prototype.getState = function() {
            return this._state
        }
        ;
        t.prototype.setVolume = function(t) {
            this._volume = t;
            return this
        }
        ;
        t.prototype.getVolume = function() {
            return this._volume
        }
        ;
        t.prototype.setMute = function(t) {
            this._mute = t;
            return this
        }
        ;
        t.prototype.getMute = function() {
            return this._mute
        }
        ;
        t.prototype.setCurrentPosition = function(t) {
            return this
        }
        ;
        t.prototype.getCurrentPosition = function() {
            return 0
        }
        ;
        t.prototype.getLoadedPercent = function() {
            return 0
        }
        ;
        t.prototype.getTotalTime = function() {
            return 0
        }
        ;
        return t
    }
    ();
    n.mixTo(i);
    return i
}
);
(function(t, e) {
    if (typeof exports === "object") {
        module.exports = e()
    } else if (typeof define === "function" && define.amd) {
        define("muplayer/lib/modernizr.audio", e)
    } else {
        t._mu.Modernizr = e()
    }
}
)(this, function() {
    return function(t, e, n) {
        var r = "2.7.1", i = {}, o = e.documentElement, s = "modernizr", u = e.createElement(s), a = u.style, f, p = {}.toString, l = {}, c = {}, h = {}, g = [], y = g.slice, d, m = {}.hasOwnProperty, _;
        if (!v(m, "undefined") && !v(m.call, "undefined")) {
            _ = function(t, e) {
                return m.call(t, e)
            }
        } else {
            _ = function(t, e) {
                return e in t && v(t.constructor.prototype[e], "undefined")
            }
        }
        if (!Function.prototype.bind) {
            Function.prototype.bind = function w(t) {
                var e = this;
                if (typeof e != "function") {
                    throw new TypeError
                }
                var n = y.call(arguments, 1)
                  , r = function() {
                    if (this instanceof r) {
                        var i = function() {}
                        ;
                        i.prototype = e.prototype;
                        var o = new i;
                        var s = e.apply(o, n.concat(y.call(arguments)));
                        if (Object(s) === s) {
                            return s
                        }
                        return o
                    } else {
                        return e.apply(t, n.concat(y.call(arguments)))
                    }
                }
                ;
                return r
            }
        }
        function E(t) {
            a.cssText = t
        }
        function T(t, e) {
            return E(prefixes.join(t + ";") + (e || ""))
        }
        function v(t, e) {
            return typeof t === e
        }
        function S(t, e) {
            return !!~("" + t).indexOf(e)
        }
        function P(t, e, r) {
            for (var i in t) {
                var o = e[t[i]];
                if (o !== n) {
                    if (r === false)
                        return t[i];
                    if (v(o, "function")) {
                        return o.bind(r || e)
                    }
                    return o
                }
            }
            return false
        }
        l["audio"] = function() {
            var t = e.createElement("audio")
              , n = false;
            try {
                if (n = !!t.canPlayType) {
                    n = new Boolean(n);
                    n.ogg = t.canPlayType('audio/ogg; codecs="vorbis"').replace(/^no$/, "");
                    n.mp3 = t.canPlayType("audio/mpeg;").replace(/^no$/, "");
                    n.wav = t.canPlayType('audio/wav; codecs="1"').replace(/^no$/, "");
                    n.m4a = (t.canPlayType("audio/x-m4a;") || t.canPlayType("audio/aac;")).replace(/^no$/, "")
                }
            } catch (r) {}
            return n
        }
        ;
        for (var R in l) {
            if (_(l, R)) {
                d = R.toLowerCase();
                i[d] = l[R]();
                g.push((i[d] ? "" : "no-") + d)
            }
        }
        i.addTest = function(t, e) {
            if (typeof t == "object") {
                for (var r in t) {
                    if (_(t, r)) {
                        i.addTest(r, t[r])
                    }
                }
            } else {
                t = t.toLowerCase();
                if (i[t] !== n) {
                    return i
                }
                e = typeof e == "function" ? e() : e;
                if (typeof enableClasses !== "undefined" && enableClasses) {
                    o.className += " " + (e ? "" : "no-") + t
                }
                i[t] = e
            }
            return i
        }
        ;
        E("");
        u = f = null ;
        i._version = r;
        return i
    }
    (this, this.document)
}
);
var __hasProp = {}.hasOwnProperty
  , __extends = function(t, e) {
    for (var n in e) {
        if (__hasProp.call(e, n))
            t[n] = e[n]
    }
    function r() {
        this.constructor = t
    }
    r.prototype = e.prototype;
    t.prototype = new r;
    t.__super__ = e.prototype;
    return t
}
  , __slice = [].slice;
(function(t, e) {
    if (typeof exports === "object") {
        return module.exports = e()
    } else if (typeof define === "function" && define.amd) {
        return define("muplayer/core/engines/audioCore", ["muplayer/core/cfg", "muplayer/core/utils", "muplayer/core/engines/engineCore", "muplayer/lib/modernizr.audio"], e)
    } else {
        return t._mu.AudioCore = e(_mu.cfg, _mu.utils, _mu.EngineCore, _mu.Modernizr)
    }
}
)(this, function(t, e, n, r) {
    var i, o, s, u, a, f, p;
    f = window;
    p = t.engine,
    a = p.TYPES,
    s = p.EVENTS,
    u = p.STATES,
    o = p.ERRCODE;
    i = function(t) {
        __extends(n, t);
        n.defaults = {
            confidence: "maybe",
            preload: false,
            autoplay: false,
            needPlayEmpty: true,
            emptyMP3: "empty.mp3"
        };
        n.prototype._supportedTypes = [];
        n.prototype.engineType = a.AUDIO;
        function n(t) {
            var e, i, o, s, a, p, l;
            this.opts = $.extend({}, n.defaults, t);
            this.opts.emptyMP3 = this.opts.baseDir + this.opts.emptyMP3;
            a = this.opts;
            s = {
                "": 0,
                maybe: 1,
                probably: 2
            };
            o = s[a.confidence];
            e = r.audio;
            if (!e) {
                return this
            }
            for (i in e) {
                l = e[i];
                if (s[l] >= o) {
                    this._supportedTypes.push(i)
                }
            }
            e = new Audio;
            e.preload = a.preload;
            e.autoplay = a.autoplay;
            e.loop = false;
            e.on = function(t, n) {
                e.addEventListener(t, n, false);
                return e
            }
            ;
            e.off = function(t, n) {
                e.removeEventListener(t, n, false);
                return e
            }
            ;
            this.audio = e;
            this._needCanPlay(["play", "setCurrentPosition"]);
            this.setState(u.STOP);
            this._initEvents();
            if (a.needPlayEmpty) {
                p = function(t) {
                    return function() {
                        if (!t.getUrl()) {
                            t.setUrl(a.emptyMP3).play()
                        }
                        return f.removeEventListener("touchstart", p, false)
                    }
                }
                (this);
                f.addEventListener("touchstart", p, false)
            }
        }
        n.prototype._test = function() {
            if (!r.audio || !this._supportedTypes.length) {
                return false
            }
            return true
        }
        ;
        n.prototype._initEvents = function() {
            var t, e, n, r, i, o, a, f;
            o = this;
            t = this.audio,
            a = this.trigger;
            f = [null , null , false],
            n = f[0],
            i = f[1],
            e = f[2];
            this.trigger = function(t, e) {
                if (o.getUrl() !== o.opts.emptyMP3) {
                    return a.call(o, t, e)
                }
            }
            ;
            r = function(t) {
                t = t || o.getLoadedPercent();
                o.trigger(s.PROGRESS, t);
                if (t === 1) {
                    clearInterval(i);
                    e = true;
                    return o.setState(u.CANPLAYTHROUGH)
                }
            }
            ;
            return t.on("loadstart", function() {
                e = false;
                clearInterval(i);
                i = setInterval(function() {
                    return r()
                }
                , 50);
                return o.setState(u.PREBUFFER)
            }
            ).on("playing", function() {
                clearTimeout(n);
                return o.setState(u.PLAYING)
            }
            ).on("pause", function() {
                return o.setState(o.getCurrentPosition() && u.PAUSE || u.STOP)
            }
            ).on("ended", function() {
                return o.setState(u.END)
            }
            ).on("error", function(t) {
                clearTimeout(n);
                return n = setTimeout(function() {
                    return o.trigger(s.ERROR, t)
                }
                , 2e3)
            }
            ).on("waiting", function() {
                return o.setState(u.PREBUFFER)
            }
            ).on("loadeddata", function() {
                return o.setState(u.BUFFERING)
            }
            ).on("timeupdate", function() {
                return o.trigger(s.POSITIONCHANGE, o.getCurrentPosition())
            }
            ).on("progress", function(t) {
                var n, o;
                clearInterval(i);
                if (!e) {
                    n = t.loaded || 0;
                    o = t.total || 1;
                    return r(n && (n / o).toFixed(2) * 1)
                }
            }
            )
        }
        ;
        n.prototype._needCanPlay = function(t) {
            var n, r, i, o, s, u;
            i = this;
            n = this.audio;
            u = [];
            for (o = 0,
            s = t.length; o < s; o++) {
                r = t[o];
                u.push(this[r] = e.wrap(this[r], function() {
                    var t, e, r;
                    e = arguments[0],
                    t = 2 <= arguments.length ? __slice.call(arguments, 1) : [];
                    if (n.readyState < 3) {
                        r = function() {
                            e.apply(i, t);
                            return n.off("canplay", r)
                        }
                        ;
                        n.on("canplay", r)
                    } else {
                        e.apply(i, t)
                    }
                    return i
                }
                ))
            }
            return u
        }
        ;
        n.prototype.play = function() {
            this.audio.play();
            return this
        }
        ;
        n.prototype.pause = function() {
            this.audio.pause();
            return this
        }
        ;
        n.prototype.stop = function() {
            try {
                this.audio.currentTime = 0
            } catch (t) {} finally {
                this.pause()
            }
            return this
        }
        ;
        n.prototype.setUrl = function(t) {
            if (t) {
                this.audio.src = t;
                this.audio.load()
            }
            return n.__super__.setUrl.call(this, t)
        }
        ;
        n.prototype.setVolume = function(t) {
            this.audio.volume = t / 100;
            return n.__super__.setVolume.call(this, t)
        }
        ;
        n.prototype.setMute = function(t) {
            this.audio.muted = t;
            return n.__super__.setMute.call(this, t)
        }
        ;
        n.prototype.setCurrentPosition = function(t) {
            try {
                this.audio.currentTime = t / 1e3
            } catch (e) {} finally {
                this.play()
            }
            return this
        }
        ;
        n.prototype.getCurrentPosition = function() {
            return this.audio.currentTime * 1e3
        }
        ;
        n.prototype.getLoadedPercent = function() {
            var t, e, n, r, i, o;
            t = this.audio;
            e = t.currentTime;
            r = t.buffered;
            if (r) {
                n = r.length;
                while (n--) {
                    if (r.start(n) <= (o = t.currentTime) && o <= r.end(n)) {
                        e = r.end(n);
                        break
                    }
                }
            }
            i = this.getTotalTime() / 1e3;
            e = e > i ? i : e;
            return i && (e / i).toFixed(2) * 1 || 0
        }
        ;
        n.prototype.getTotalTime = function() {
            var t, e, n, r, i;
            i = this.audio,
            r = i.duration,
            e = i.buffered,
            n = i.currentTime;
            r = ~~r;
            if (r === 0 && e) {
                t = e.length;
                if (t > 0) {
                    r = e.end(--t)
                } else {
                    r = n
                }
            }
            return r && r * 1e3 || 0
        }
        ;
        return n
    }
    (n);
    return i
}
);
(function(t, e) {
    if (typeof exports === "object") {
        module.exports = e()
    } else if (typeof define === "function" && define.amd) {
        define("muplayer/lib/jquery.swfobject", e)
    } else {
        e()
    }
}
)(this, function() {
    (function(t, e, n) {
        var r = "object"
          , i = true;
        function o(t, e) {
            var n = (t[0] || 0) - (e[0] || 0);
            return n > 0 || !n && t.length > 0 && o(t.slice(1), e.slice(1))
        }
        function s(t) {
            if (typeof t != r) {
                return t
            }
            var e = []
              , n = "";
            for (var o in t) {
                if (typeof t[o] == r) {
                    n = s(t[o])
                } else {
                    n = [o, i ? encodeURI(t[o]) : t[o]].join("=")
                }
                e.push(n)
            }
            return e.join("&")
        }
        function u(t) {
            var e = [];
            for (var n in t) {
                if (t[n]) {
                    e.push([n, '="', t[n], '"'].join(""))
                }
            }
            return e.join(" ")
        }
        function a(t) {
            var e = [];
            for (var n in t) {
                e.push(['<param name="', n, '" value="', s(t[n]), '" />'].join(""))
            }
            return e.join("")
        }
        try {
            var f = n.description || function() {
                return new n("ShockwaveFlash.ShockwaveFlash").GetVariable("$version")
            }
            ()
        } catch (p) {
            f = "Unavailable"
        }
        var l = f.match(/\d+/g) || [0];
        t[e] = {
            available: l[0] > 0,
            activeX: n && !n.name,
            version: {
                original: f,
                array: l,
                string: l.join("."),
                major: parseInt(l[0], 10) || 0,
                minor: parseInt(l[1], 10) || 0,
                release: parseInt(l[2], 10) || 0
            },
            hasVersion: function(t) {
                var e = /string|number/.test(typeof t) ? t.toString().split(".") : /object/.test(typeof t) ? [t.major, t.minor] : t || [0, 0];
                return o(l, e)
            },
            encodeParams: true,
            expressInstall: "expressInstall.swf",
            expressInstallIsActive: false,
            create: function(t) {
                var e = this;
                if (!t.swf || e.expressInstallIsActive || !e.available && !t.hasVersionFail) {
                    return false
                }
                if (!e.hasVersion(t.hasVersion || 1)) {
                    e.expressInstallIsActive = true;
                    if (typeof t.hasVersionFail == "function") {
                        if (!t.hasVersionFail.apply(t)) {
                            return false
                        }
                    }
                    t = {
                        swf: t.expressInstall || e.expressInstall,
                        height: 137,
                        width: 214,
                        flashvars: {
                            MMredirectURL: location.href,
                            MMplayerType: e.activeX ? "ActiveX" : "PlugIn",
                            MMdoctitle: document.title.slice(0, 47) + " - Flash Player Installation"
                        }
                    }
                }
                attrs = {
                    data: t.swf,
                    type: "application/x-shockwave-flash",
                    id: t.id || "flash_" + Math.floor(Math.random() * 999999999),
                    width: t.width || 320,
                    height: t.height || 180,
                    style: t.style || ""
                };
                i = typeof t.useEncode !== "undefined" ? t.useEncode : e.encodeParams;
                t.movie = t.swf;
                t.wmode = t.wmode || "opaque";
                delete t.fallback;
                delete t.hasVersion;
                delete t.hasVersionFail;
                delete t.height;
                delete t.id;
                delete t.swf;
                delete t.useEncode;
                delete t.width;
                var n = document.createElement("div");
                n.innerHTML = ["<object ", u(attrs), ">", a(t), "</object>"].join("");
                return n.firstChild
            }
        };
        t.fn[e] = function(n) {
            var i = this.find(r).andSelf().filter(r);
            if (/string|object/.test(typeof n)) {
                this.each(function() {
                    var i = t(this), o;
                    n = typeof n == r ? n : {
                        swf: n
                    };
                    n.fallback = this;
                    o = t[e].create(n);
                    if (o) {
                        i.children().remove();
                        i.html(o)
                    }
                }
                )
            }
            if (typeof n == "function") {
                i.each(function() {
                    var r = this
                      , i = "jsInteractionTimeoutMs";
                    r[i] = r[i] || 0;
                    if (r[i] < 660) {
                        if (r.clientWidth || r.clientHeight) {
                            n.call(r)
                        } else {
                            setTimeout(function() {
                                t(r)[e](n)
                            }
                            , r[i] + 66)
                        }
                    }
                }
                )
            }
            return i
        }
    }
    )(jQuery, "flash", navigator.plugins["Shockwave Flash"] || window.ActiveXObject)
}
);
var __hasProp = {}.hasOwnProperty
  , __extends = function(t, e) {
    for (var n in e) {
        if (__hasProp.call(e, n))
            t[n] = e[n]
    }
    function r() {
        this.constructor = t
    }
    r.prototype = e.prototype;
    t.prototype = new r;
    t.__super__ = e.prototype;
    return t
}
  , __slice = [].slice;
(function(t, e) {
    if (typeof exports === "object") {
        return module.exports = e()
    } else if (typeof define === "function" && define.amd) {
        return define("muplayer/core/engines/flashCore", ["muplayer/core/cfg", "muplayer/core/utils", "muplayer/lib/Timer", "muplayer/core/engines/engineCore", "muplayer/lib/jquery.swfobject"], e)
    } else {
        return t._mu.FlashCore = e(_mu.cfg, _mu.utils, _mu.Timer, _mu.EngineCore)
    }
}
)(this, function(t, e, n, r) {
    var i, o, s, u, a, f, p;
    p = t.engine,
    o = p.EVENTS,
    u = p.STATES,
    i = p.ERRCODE;
    f = t.timerResolution;
    a = {
        1: u.CANPLAYTHROUGH,
        2: u.PREBUFFER,
        3: u.BUFFERING,
        4: u.PLAYING,
        5: u.PAUSE,
        6: u.STOP,
        7: u.END
    };
    s = function(t) {
        __extends(r, t);
        r.defaults = {
            swfCacheTime: .5 * 3600 * 1e3,
            expressInstaller: "expressInstall.swf"
        };
        function r(t) {
            var n, i, o, s;
            this.opts = s = $.extend({}, r.defaults, this.defaults, t);
            this._state = u.STOP;
            this._loaded = false;
            this._queue = [];
            this._needFlashReady(["play", "pause", "stop", "setCurrentPosition", "_setUrl", "_setVolume", "_setMute"]);
            this._unexceptionGet(["getCurrentPosition", "getLoadedPercent", "getTotalTime"]);
            n = s.baseDir;
            i = "muplayer_" + setTimeout(function() {}
            , 0);
            o = s.instanceName + "_" + i;
            e.namespace("engines")[o] = this;
            o = "_mu.engines." + o;
            this.flash = $.flash.create({
                swf: n + s.swf + "?t=" + Math.floor(+new Date / s.swfCacheTime),
                id: i,
                height: 1,
                width: 1,
                allowscriptaccess: "always",
                wmode: "transparent",
                expressInstaller: n + s.expressInstaller,
                flashvars: {
                    _instanceName: o,
                    _buffertime: 5e3
                }
            });
            this.flash.tabIndex = -1;
            s.$el.append(this.flash);
            this._initEvents()
        }
        r.prototype._test = function() {
            if (!this.flash || !$.flash.hasVersion(this.opts.flashVer)) {
                return false
            }
            return true
        }
        ;
        r.prototype._initEvents = function() {
            var t, e, r;
            t = this;
            this.progressTimer = new n(f);
            this.positionTimer = new n(f);
            r = function() {
                var e;
                e = t.getLoadedPercent();
                if (t._lastPer !== e) {
                    t._lastPer = e;
                    t.trigger(o.PROGRESS, e)
                }
                if (e === 1) {
                    return t.progressTimer.stop()
                }
            }
            ;
            e = function() {
                var e;
                e = t.getCurrentPosition();
                if (t._lastPos !== e) {
                    t._lastPos = e;
                    return t.trigger(o.POSITIONCHANGE, e)
                }
            }
            ;
            this.progressTimer.every("100 ms", r);
            this.positionTimer.every("100 ms", e);
            return this.on(o.STATECHANGE, function(n) {
                var r;
                r = n.newState;
                switch (r) {
                case u.PREBUFFER:
                case u.PLAYING:
                    t.progressTimer.start();
                    break;
                case u.PAUSE:
                case u.STOP:
                    t.progressTimer.stop();
                    break;
                case u.END:
                    t.progressTimer.reset()
                }
                switch (r) {
                case u.PLAYING:
                    return t.positionTimer.start();
                case u.PAUSE:
                case u.STOP:
                    t.positionTimer.stop();
                    return e();
                case u.END:
                    return t.positionTimer.reset()
                }
            }
            )
        }
        ;
        r.prototype._needFlashReady = function(t) {
            var n, r, i, o;
            o = [];
            for (r = 0,
            i = t.length; r < i; r++) {
                n = t[r];
                o.push(this[n] = e.wrap(this[n], function(t) {
                    return function() {
                        var e, n;
                        n = arguments[0],
                        e = 2 <= arguments.length ? __slice.call(arguments, 1) : [];
                        if (t._loaded) {
                            n.apply(t, e)
                        } else {
                            t._pushQueue(n, e)
                        }
                        return t
                    }
                }
                (this)))
            }
            return o
        }
        ;
        r.prototype._unexceptionGet = function(t) {
            var n, r, i, o;
            o = [];
            for (r = 0,
            i = t.length; r < i; r++) {
                n = t[r];
                o.push(this[n] = e.wrap(this[n], function(t) {
                    return function() {
                        var e, n;
                        n = arguments[0],
                        e = 2 <= arguments.length ? __slice.call(arguments, 1) : [];
                        try {
                            return n.apply(t, e)
                        } catch (r) {
                            return 0
                        }
                    }
                }
                (this)))
            }
            return o
        }
        ;
        r.prototype._pushQueue = function(t, e) {
            return this._queue.push([t, e])
        }
        ;
        r.prototype._fireQueue = function() {
            var t, e, n, r;
            r = [];
            while (this._queue.length) {
                n = this._queue.shift(),
                e = n[0],
                t = n[1];
                r.push(e.apply(this, t))
            }
            return r
        }
        ;
        r.prototype.play = function() {
            this.flash.f_play();
            return this
        }
        ;
        r.prototype.pause = function() {
            this.flash.f_pause();
            return this
        }
        ;
        r.prototype.stop = function() {
            this.flash.f_stop();
            return this
        }
        ;
        r.prototype._setUrl = function(t) {
            return this.flash.f_load(t)
        }
        ;
        r.prototype.setUrl = function(t) {
            var e;
            e = this;
            if (t) {
                this._setUrl(t);
                (function() {
                    var t, n;
                    n = null ;
                    t = function(r) {
                        if (r.newState === u.PLAYING && r.oldState === u.PREBUFFER) {
                            return n = setTimeout(function() {
                                e.off(o.STATECHANGE, t);
                                if (e.getCurrentPosition() < 100) {
                                    e.setState(u.END);
                                    return e.trigger(o.ERROR, i.MEDIA_ERR_SRC_NOT_SUPPORTED)
                                }
                            }
                            , 2e3)
                        } else {
                            return clearTimeout(n)
                        }
                    }
                    ;
                    return e.off(o.STATECHANGE, t).on(o.STATECHANGE, t)
                }
                )()
            }
            return r.__super__.setUrl.call(this, t)
        }
        ;
        r.prototype.getState = function() {
            return this._state
        }
        ;
        r.prototype._setVolume = function(t) {
            return this.flash.setData("volume", t)
        }
        ;
        r.prototype.setVolume = function(t) {
            this._setVolume(t);
            return r.__super__.setVolume.call(this, t)
        }
        ;
        r.prototype._setMute = function(t) {
            return this.flash.setData("mute", t)
        }
        ;
        r.prototype.setMute = function(t) {
            this._setMute(t);
            return r.__super__.setMute.call(this, t)
        }
        ;
        r.prototype.setCurrentPosition = function(t) {
            this.flash.f_play(t);
            return this
        }
        ;
        r.prototype.getCurrentPosition = function() {
            return this.flash.getData("position")
        }
        ;
        r.prototype.getLoadedPercent = function() {
            return this.flash.getData("loadedPct")
        }
        ;
        r.prototype.getTotalTime = function() {
            return this.flash.getData("length")
        }
        ;
        r.prototype._swfOnLoad = function() {
            this._loaded = true;
            return setTimeout(function(t) {
                return function() {
                    return t._fireQueue()
                }
            }
            (this), 0)
        }
        ;
        r.prototype._swfOnStateChange = function(t) {
            var e;
            e = a[t];
            if (e) {
                return this.setState(e)
            }
        }
        ;
        r.prototype._swfOnErr = function(t) {
            this.setState(u.END);
            this.trigger(o.ERROR);
            return typeof console !== "undefined" && console !== null  ? typeof console.error === "function" ? console.error(t) : void 0 : void 0
        }
        ;
        return r
    }
    (r);
    return s
}
);
var __hasProp = {}.hasOwnProperty
  , __extends = function(t, e) {
    for (var n in e) {
        if (__hasProp.call(e, n))
            t[n] = e[n]
    }
    function r() {
        this.constructor = t
    }
    r.prototype = e.prototype;
    t.prototype = new r;
    t.__super__ = e.prototype;
    return t
}
;
(function(t, e) {
    if (typeof exports === "object") {
        return module.exports = e()
    } else if (typeof define === "function" && define.amd) {
        return define("muplayer/core/engines/flashMP3Core", ["muplayer/core/cfg", "muplayer/core/engines/flashCore"], e)
    } else {
        return t._mu.FlashMP3Core = e(_mu.cfg, _mu.FlashCore)
    }
}
)(this, function(t, e) {
    var n, r;
    r = t.engine.TYPES;
    n = function(t) {
        __extends(e, t);
        function e() {
            return e.__super__.constructor.apply(this, arguments)
        }
        e.prototype.defaults = {
            swf: "muplayer_mp3.swf",
            instanceName: "MP3Core",
            flashVer: "9.0.0"
        };
        e.prototype._supportedTypes = ["mp3"];
        e.prototype.engineType = r.FLASH_MP3;
        return e
    }
    (e);
    return n
}
);
var __hasProp = {}.hasOwnProperty
  , __extends = function(t, e) {
    for (var n in e) {
        if (__hasProp.call(e, n))
            t[n] = e[n]
    }
    function r() {
        this.constructor = t
    }
    r.prototype = e.prototype;
    t.prototype = new r;
    t.__super__ = e.prototype;
    return t
}
;
(function(t, e) {
    if (typeof exports === "object") {
        return module.exports = e()
    } else if (typeof define === "function" && define.amd) {
        return define("muplayer/core/engines/flashMP4Core", ["muplayer/core/cfg", "muplayer/core/engines/flashCore"], e)
    } else {
        return t._mu.FlashMP4Core = e(_mu.cfg, _mu.FlashCore)
    }
}
)(this, function(t, e) {
    var n, r;
    r = t.engine.TYPES;
    n = function(t) {
        __extends(e, t);
        function e() {
            return e.__super__.constructor.apply(this, arguments)
        }
        e.prototype.defaults = {
            swf: "muplayer_mp4.swf",
            instanceName: "MP4Core",
            flashVer: "9.0.115"
        };
        e.prototype._supportedTypes = ["m4a"];
        e.prototype.engineType = r.FLASH_MP4;
        return e
    }
    (e);
    return n
}
);
(function(t, e) {
    if (typeof exports === "object") {
        return module.exports = e()
    } else if (typeof define === "function" && define.amd) {
        return define("muplayer/core/engines/engine", ["muplayer/core/cfg", "muplayer/core/utils", "muplayer/lib/events", "muplayer/core/engines/engineCore", "muplayer/core/engines/audioCore", "muplayer/core/engines/flashMP3Core", "muplayer/core/engines/flashMP4Core"], e)
    } else {
        return t._mu.Engine = e(_mu.cfg, _mu.utils, _mu.Events, _mu.EngineCore, _mu.AudioCore, _mu.FlashMP3Core, _mu.FlashMP4Core)
    }
}
)(this, function(cfg, utils, Events, EngineCore, AudioCore, FlashMP3Core, FlashMP4Core) {
    var EVENTS, Engine, STATES, extReg, timerResolution, _ref;
    _ref = cfg.engine,
    EVENTS = _ref.EVENTS,
    STATES = _ref.STATES;
    timerResolution = cfg.timerResolution;
    extReg = /\.(\w+)(\?.*)?$/;
    Engine = function() {
        Engine.el = '<div id="muplayer_container_{{DATETIME}}" style="width: 1px; height: 1px; overflow: hidden"></div>';
        Engine.prototype.defaults = {
            engines: [{
                constructor: FlashMP3Core
            }, {
                constructor: FlashMP4Core
            }, {
                constructor: AudioCore
            }]
        };
        function Engine(t) {
            this.opts = $.extend({}, this.defaults, t);
            this._initEngines()
        }
        Engine.prototype._initEngines = function() {
            var $el, args, constructor, engine, i, opts, _i, _len, _ref1;
            this.engines = [];
            opts = this.opts;
            $el = $(Engine.el.replace(/{{DATETIME}}/g, +new Date)).appendTo("body");
            _ref1 = opts.engines;
            for (i = _i = 0,
            _len = _ref1.length; _i < _len; i = ++_i) {
                engine = _ref1[i];
                constructor = engine.constructor;
                args = engine.args || {};
                args.baseDir = opts.baseDir;
                args.$el = $el;
                try {
                    if (!$.isFunction(constructor)) {
                        constructor = eval(constructor)
                    }
                    engine = new constructor(args)
                } catch (_error) {
                    throw "Missing constructor: " + String(engine.constructor)
                }
                if (engine._test()) {
                    this.engines.push(engine)
                }
            }
            if (this.engines.length) {
                return this.setEngine(this.engines[0])
            } else {
                return this.setEngine(new EngineCore)
            }
        }
        ;
        Engine.prototype.setEngine = function(t) {
            var e, n, r, i, o, s, u, a;
            s = this;
            this._lastE = {};
            u = function(t) {
                var e, n;
                e = t.newState,
                n = t.oldState;
                if (n === s._lastE.oldState && e === s._lastE.newState) {
                    return
                }
                s._lastE = {
                    oldState: n,
                    newState: e
                };
                s.trigger(EVENTS.STATECHANGE, t);
                if (e === STATES.CANPLAYTHROUGH && (n === STATES.PLAYING || n === STATES.PAUSE)) {
                    return s.setState(n)
                }
            }
            ;
            i = function(t) {
                return s.trigger(EVENTS.POSITIONCHANGE, t)
            }
            ;
            o = function(t) {
                return s.trigger(EVENTS.PROGRESS, t)
            }
            ;
            n = function(t) {
                return s.trigger(EVENTS.ERROR, t)
            }
            ;
            e = function(t) {
                return t.on(EVENTS.STATECHANGE, u).on(EVENTS.POSITIONCHANGE, i).on(EVENTS.PROGRESS, o).on(EVENTS.ERROR, n)
            }
            ;
            a = function(t) {
                return t.off(EVENTS.STATECHANGE, u).off(EVENTS.POSITIONCHANGE, i).off(EVENTS.PROGRESS, o).off(EVENTS.ERROR, n)
            }
            ;
            if (!this.curEngine) {
                return this.curEngine = e(t)
            } else if (this.curEngine !== t) {
                r = this.curEngine;
                a(r).reset();
                this.curEngine = e(t);
                return this.curEngine.setVolume(r.getVolume()).setMute(r.getMute())
            }
        }
        ;
        Engine.prototype.canPlayType = function(t) {
            return $.inArray(t, this.getSupportedTypes()) !== -1
        }
        ;
        Engine.prototype.getSupportedTypes = function() {
            var t, e, n, r, i;
            e = [];
            i = this.engines;
            for (n = 0,
            r = i.length; n < r; n++) {
                t = i[n];
                e = e.concat(t.getSupportedTypes())
            }
            return e
        }
        ;
        Engine.prototype.switchEngineByType = function(t) {
            var e, n, r, i, o;
            n = false;
            o = this.engines;
            for (r = 0,
            i = o.length; r < i; r++) {
                e = o[r];
                if (e.canPlayType(t)) {
                    this.setEngine(e);
                    n = true;
                    break
                }
            }
            if (!n) {
                return this.setEngine(this.engines[0])
            }
        }
        ;
        Engine.prototype.reset = function() {
            this.curEngine.reset();
            return this
        }
        ;
        Engine.prototype.setUrl = function(t) {
            var e;
            if (extReg.test(t)) {
                e = RegExp.$1.toLocaleLowerCase()
            }
            if (this.canPlayType(e)) {
                if (!this.curEngine.canPlayType(e)) {
                    this.switchEngineByType(e)
                }
            } else {
                throw new Error("Can not play with: " + e)
            }
            this.curEngine.setUrl(t);
            return this
        }
        ;
        Engine.prototype.getUrl = function() {
            return this.curEngine.getUrl()
        }
        ;
        Engine.prototype.play = function() {
            this.curEngine.play();
            return this
        }
        ;
        Engine.prototype.pause = function() {
            this.curEngine.pause();
            this.trigger(EVENTS.POSITIONCHANGE, this.getCurrentPosition());
            this.setState(STATES.PAUSE);
            return this
        }
        ;
        Engine.prototype.stop = function() {
            this.curEngine.stop();
            this.trigger(EVENTS.POSITIONCHANGE, 0);
            this.setState(STATES.STOP);
            return this
        }
        ;
        Engine.prototype.setState = function(t) {
            this.curEngine.setState(t);
            return this
        }
        ;
        Engine.prototype.getState = function() {
            return this.curEngine.getState()
        }
        ;
        Engine.prototype.setMute = function(t) {
            this.curEngine.setMute(!!t);
            return this
        }
        ;
        Engine.prototype.getMute = function() {
            return this.curEngine.getMute()
        }
        ;
        Engine.prototype.setVolume = function(t) {
            if ($.isNumeric(t) && t >= 0 && t <= 100) {
                this.curEngine.setVolume(t)
            }
            return this
        }
        ;
        Engine.prototype.getVolume = function() {
            return this.curEngine.getVolume()
        }
        ;
        Engine.prototype.setCurrentPosition = function(t) {
            t = ~~t;
            this.curEngine.setCurrentPosition(t);
            return this
        }
        ;
        Engine.prototype.getCurrentPosition = function() {
            return this.curEngine.getCurrentPosition()
        }
        ;
        Engine.prototype.getLoadedPercent = function() {
            return this.curEngine.getLoadedPercent()
        }
        ;
        Engine.prototype.getTotalTime = function() {
            return this.curEngine.getTotalTime()
        }
        ;
        Engine.prototype.getEngineType = function() {
            return this.curEngine.engineType
        }
        ;
        Engine.prototype.getState = function() {
            return this.curEngine.getState()
        }
        ;
        return Engine
    }
    ();
    Events.mixTo(Engine);
    return Engine
}
);
(function(t, e) {
    if (typeof exports === "object") {
        return module.exports = e()
    } else if (typeof define === "function" && define.amd) {
        return define("muplayer/player", ["muplayer/core/cfg", "muplayer/core/utils", "muplayer/lib/Timer", "muplayer/lib/events", "muplayer/core/playlist", "muplayer/core/engines/engine"], e)
    } else {
        return t._mu.Player = e(_mu.cfg, _mu.utils, _mu.Timer, _mu.Events, _mu.Playlist, _mu.Engine)
    }
}
)(this, function(t, e, n, r, i, o) {
    var s, u, a, f, p, l;
    l = t.engine,
    s = l.EVENTS,
    a = l.STATES;
    p = e.time2str;
    f = function(t, e) {
        var n, r;
        if (t !== "prev" && t !== "next") {
            return this
        }
        this.stop();
        n = this.playlist;
        r = function(n) {
            return function() {
                var r;
                r = {
                    cur: n.getCur()
                };
                if (e) {
                    r.auto = e
                }
                n.trigger("player:" + t, r);
                return n.play()
            }
        }
        (this);
        if (this.getSongsNum()) {
            if (!n.cur) {
                r()
            } else if (n[t]()) {
                r()
            }
        }
        return this
    }
    ;
    u = function() {
        var e;
        e = null ;
        r.defaults = {
            baseDir: "" + t.cdn + t.version,
            mode: "loop",
            mute: false,
            volume: 80,
            singleton: true,
            absoluteUrl: true,
            maxRetryTimes: 1,
            maxWaitingTime: 4 * 1e3,
            recoverMethodWhenWaitingTimeout: "retry"
        };
        function r(t) {
            var s, u;
            this.opts = u = $.extend({}, r.defaults, t);
            this.waitingTimer = new n(100);
            s = u.baseDir;
            if (s === false) {
                s = ""
            } else if (!s) {
                throw "baseDir must be set! Usually, it should point to the MuPlayer's dist directory."
            }
            if (s && !s.endsWith("/")) {
                s = s + "/"
            }
            if (u.singleton) {
                if (e) {
                    return e
                }
                e = this
            }
            this.playlist = new i({
                absoluteUrl: u.absoluteUrl
            });
            this.playlist.setMode(u.mode);
            this._initEngine(new o({
                baseDir: s,
                engines: u.engines
            }));
            this.setMute(u.mute);
            this.setVolume(u.volume);
            this.reset()
        }
        r.prototype._initEngine = function(t) {
            var e, n, r;
            r = this;
            e = this.opts;
            n = e.recoverMethodWhenWaitingTimeout;
            return this.engine = t.on(s.STATECHANGE, function(t) {
                var e;
                e = t.newState;
                if (e !== a.PREBUFFER && e !== a.BUFFERING) {
                    r.waitingTimer.clear()
                }
                r.trigger("player:statechange", t);
                r.trigger(e);
                if (e === a.END) {
                    return r.next(true)
                }
            }
            ).on(s.POSITIONCHANGE, function(n) {
                r.trigger("timeupdate", n);
                if (r.getUrl()) {
                    return r.waitingTimer.clear().after(e.maxWaitingTime, function() {
                        var e;
                        if ((e = r.getState()) !== a.PAUSE && e !== a.STOP && e !== a.END) {
                            return t.trigger(s.WAITING_TIMEOUT)
                        }
                    }
                    ).start()
                }
            }
            ).on(s.PROGRESS, function(t) {
                return r.trigger("progress", t)
            }
            ).on(s.ERROR, function(t) {
                if (typeof console !== "undefined" && console !== null ) {
                    if (typeof console.error === "function") {
                        console.error("error: ", t)
                    }
                }
                return r.trigger("error", t)
            }
            ).on(s.WAITING_TIMEOUT, function() {
                if (n === "retry" || n === "next") {
                    r[n]()
                }
                return r.trigger("player:waiting_timeout")
            }
            )
        }
        ;
        r.prototype.retry = function() {
            var t, e;
            if (this._retryTimes < this.opts.maxRetryTimes) {
                this._retryTimes++;
                e = this.getUrl();
                t = this.engine.getCurrentPosition();
                this.pause().setUrl(e).engine.setCurrentPosition(t);
                this.trigger("player:retry", this._retryTimes)
            } else {
                this.trigger("player:retry:max")
            }
            return this
        }
        ;
        r.prototype.play = function(t) {
            var e, n, r, i, o;
            i = this;
            n = this.engine;
            e = $.Deferred();
            t = ~~t;
            r = function() {
                return setTimeout(function() {
                    if (i.getUrl()) {
                        if (t) {
                            n.setCurrentPosition(t)
                        } else {
                            n.play()
                        }
                    }
                    i.trigger("player:play", t);
                    return e.resolve()
                }
                , 0)
            }
            ;
            o = this.getState();
            if (o === a.STOP || o === a.END || o === a.BUFFERING && this.curPos() === 0) {
                this.trigger("player:fetch:start");
                this._fetch().done(function() {
                    i.trigger("player:fetch:done");
                    return r()
                }
                )
            } else {
                r()
            }
            return e.promise()
        }
        ;
        r.prototype.pause = function() {
            this.engine.pause();
            this.trigger("player:pause");
            return this
        }
        ;
        r.prototype.stop = function() {
            this.engine.stop();
            this.trigger("player:stop");
            return this
        }
        ;
        r.prototype.replay = function() {
            return this.stop().play()
        }
        ;
        r.prototype.prev = function() {
            return f.apply(this, ["prev"])
        }
        ;
        r.prototype.next = function(t) {
            return f.apply(this, ["next", t])
        }
        ;
        r.prototype.getCur = function() {
            var t, e;
            e = this.playlist;
            t = e.cur;
            if (!t && this.getSongsNum()) {
                t = e.list[0];
                e.setCur(t)
            }
            return t + ""
        }
        ;
        r.prototype.setCur = function(t) {
            var e;
            e = this.playlist;
            if (!t && this.getSongsNum()) {
                t = e.list[0]
            }
            if (t && this._sid !== t) {
                e.setCur(t);
                this._sid = t;
                this.stop()
            }
            return this
        }
        ;
        r.prototype.curPos = function(t) {
            var e;
            e = this.engine.getCurrentPosition() / 1e3;
            if (t) {
                return p(e)
            } else {
                return e
            }
        }
        ;
        r.prototype.duration = function(t) {
            var e;
            e = this.engine.getTotalTime() / 1e3;
            if (t) {
                return p(e)
            } else {
                return e
            }
        }
        ;
        r.prototype.add = function(t) {
            if (t) {
                this.playlist.add(t)
            }
            this.trigger("player:add", t);
            return this
        }
        ;
        r.prototype.remove = function(t) {
            if (t) {
                this.playlist.remove(t)
            }
            if (!this.getSongsNum()) {
                this.reset()
            }
            this.trigger("player:remove", t);
            return this
        }
        ;
        r.prototype.reset = function() {
            this._retryTimes = 0;
            this.playlist.reset();
            this.engine.reset();
            this.trigger("player:reset");
            return this
        }
        ;
        r.prototype.getState = function() {
            return this.engine.getState()
        }
        ;
        r.prototype.setUrl = function(t) {
            if (!t) {
                return this
            }
            this.stop().engine.setUrl(t);
            this.trigger("player:setUrl", t);
            return this
        }
        ;
        r.prototype.getUrl = function() {
            return this.engine.getUrl()
        }
        ;
        r.prototype.setVolume = function(t) {
            this.engine.setVolume(t);
            this.trigger("player:setVolume", t);
            return this
        }
        ;
        r.prototype.getVolume = function() {
            return this.engine.getVolume()
        }
        ;
        r.prototype.setMute = function(t) {
            this.engine.setMute(t);
            this.trigger("player:setMute", t);
            return this
        }
        ;
        r.prototype.getMute = function() {
            return this.engine.getMute()
        }
        ;
        r.prototype.canPlayType = function(t) {
            return this.engine.canPlayType(t)
        }
        ;
        r.prototype.getSongsNum = function() {
            return this.playlist.list.length
        }
        ;
        r.prototype.setMode = function(t) {
            this.playlist.setMode(t);
            this.trigger("player:setMode", t);
            return this
        }
        ;
        r.prototype.getMode = function() {
            return this.playlist.mode
        }
        ;
        r.prototype.getEngineType = function() {
            return this.engine.curEngine.engineType
        }
        ;
        r.prototype._fetch = function() {
            var t, e;
            e = $.Deferred();
            t = this.getCur();
            if (this.getUrl() === t) {
                e.resolve()
            } else {
                setTimeout(function(n) {
                    return function() {
                        n.setUrl(t);
                        return e.resolve()
                    }
                }
                (this), 0)
            }
            return e.promise()
        }
        ;
        return r
    }
    ();
    r.mixTo(u);
    return u
}
);
