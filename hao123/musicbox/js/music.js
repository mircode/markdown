__d("widget.musicbox.marquee", ["common.js.widget"], function(g, h, e, c) {
    c("common.js.widget");
    $.widget("ui.marquee", {
        options: {
            speed: 100,
            marginLeft: "0px",
            orientation: "left"
        },
        _create: function() {
            var d = $("<div></div>");
            d.css({
                overflow: "hidden",
                "float": "left",
                height: "20px",
                marginLeft: this.options.marginLeft
            });
            this.element.wrap(d);
            this.wrapper = this.element.parent();
            this.parentWidth = this.wrapperWidth = this.elemWidth = null ;
            this.isLeft = !0;
            this.time = null ;
            this.cloned = !1
        },
        _init: function() {
            this.start()
        },
        _scrollround: function() {
            var d = 
            this.wrapper.scrollLeft();
            d + this.wrapperWidth == this.elemWidth ? this.isLeft = !1 : 0 == d && (this.isLeft = !0);
            this.wrapper.scrollLeft(this.isLeft ? d + 1 : d - 1)
        },
        _scrollleft: function() {
            var d = this.wrapper.scrollLeft();
            d + this.wrapperWidth == this.elemWidth ? this.wrapper.scrollLeft(1) : this.wrapper.scrollLeft(d + 1)
        },
        _onWrapperEvent: function() {
            var d = this;
            this.wrapper.on("mouseenter", function() {
                d._pause()
            }
            );
            this.wrapper.on("mouseleave", function() {
                d._start()
            }
            )
        },
        _offWrapperEvent: function() {
            this._stop();
            this.wrapper.off()
        },
        _start: function() {
            var d = 
            this;
            if (!this.time) {
                var c = "_scroll" + this.options.orientation;
                this.time = setInterval(function() {
                    d[c]()
                }
                , this.options.speed)
            }
        },
        _pause: function() {
            clearInterval(this.time);
            this.time = null 
        },
        _stop: function() {
            this._pause();
            this.wrapper.scrollLeft(0)
        },
        _needMarquee: function() {
            this.wrapperWidth = this.parentWidth = this.wrapper.parent().width();
            this.elemWidth = this.element.width();
            return this.elemWidth > this.wrapperWidth
        },
        start: function() {
            this._needMarquee() ? (this._onWrapperEvent(),
            "left" == this.options.orientation && 
            this._cloneDom(),
            this.wrapper.width(this.wrapperWidth),
            this._start()) : (this._offWrapperEvent(),
            this.wrapper.width(this.elemWidth))
        },
        _cloneDom: function() {
            this.elemWidth = this.elemWidth + this.wrapperWidth + 20;
            if (!this.cloned) {
                var d = 2 * this.elemWidth + 20;
                this.element.clone(!0).appendTo(this.wrapper).css({
                    marginLeft: "20px"
                });
                var c = $("<div></div>");
                c.css({
                    overflow: "hidden",
                    width: d
                });
                this.wrapper.wrapInner(c);
                this.cloned = !0
            }
        },
        _destroy: function() {
            this._offWrapperEvent();
            $.widget.prototype.destroy.call(this)
        }
    });
    return e
}
);
__d("widget.musicbox.musicbox", ["common.js.jquery-ui", "widget.musicbox.marquee", "common.js.format", "common.js.events"], function(g, h, e, c) {
    c("common.js.jquery-ui");
    c("widget.musicbox.marquee");
    var d = c("common.js.format")
      , l = c("common.js.events");
    $.widget("hao123.musicbox", {
        options: {
            tpl: "<div class='title'><a class='name' href='http://music.baidu.com/song/#{songid}'>#{name}</a><span class='split'>-</span><a class='author' href='http://music.baidu.com/artist/#{uid}'>#{author}</a></div>",
            hotUrl: "https://gss2.baidu.com/6Ls1aze90MgYm2Gp8IqW0jdnxx1xbK/v1/restserver/ting?method=baidu.ting.billboard.billList&type=2&from=ext&client=hao123",
            playUrl: "https://gss2.baidu.com/6Ls1aze90MgYm2Gp8IqW0jdnxx1xbK/v1/restserver/ting?method=baidu.ting.song.play&from=ext&client=hao123",
            playerUrl: "https://gss1.bdstatic.com/5eN1dDebRNRTm2_p8IuM_a/res/muPlayer/player.min.js",
            baseDir: "https://gss0.bdstatic.com/5eR1dDebRNRTm2_p8IuM_a/res/muPlayer/",
            mp3Url: "http://play.baidu.com/?__m=mboxCtrl.playSong&__a=#{id}&__o=||hao123_player",
            playingCls: "playing",
            volMidCls: "vol-mid",
            volLargeCls: "vol-large",
            volMuteCls: "vol-mute"
        },
        _create: function() {
            var a = this.element;
            this.$info = $(".info-hook", a);
            this.$play = $(".play-hook", a);
            this.$next = $(".next-hook", a);
            this.$prev = $(".prev-hook", a);
            this.$open = $(".open-hook", 
            a);
            this.$vol = $(".vol-hook", a);
            this.$volSilder = $(".vol-slider-hook", a);
            // 播放进度
            this.$proSilder = $(".pro-slider-hook", a);
            this.player = null ;
            this.playList = {};
            this.playing = !1;
            this.volume = 60
        },
        _init: function() {
            var a = this;
            this._initPlayer(function() {
                a._getPlayList(function() {
                    a._initVolume();
                    a._initProcess();
                    a._bindEvent();
                }
                )
            }
            )
        },
        _bindEvent: function() {
            var a = this
              , b = a.options;
            this.$vol.on("click", function() {
                a.player.getMute() ? ($(this).removeClass(b.volMuteCls),
                a.player.setMute(!1)) : ($(this).addClass(b.volMuteCls),
                a.player.setMute(!0));
                return !1
            }
            );
            this.$play.on("click", function() {
                a.playing ? 
                (a.playing = !1,
                a.player.pause(),
                a.$play.removeClass(b.playingCls)) : (a.playing = !0,
                a.player.play(),
                a.$play.addClass(b.playingCls));
                return !1
            }
            );
            this.$prev.on("click", function() {
                a.player.prev();
                return !1
            }
            );
            this.$next.on("click", function() {
                a.player.next();
                return !1
            }
            );
            this.$open.on("click", function() {
                a.playing && (a.playing = !1,
                a.player.pause(),
                a.$play.removeClass(b.playingCls))
            }
            );
            this.player.on("player:play", function() {
                a.playing = !0;
                a.$play.addClass(b.playingCls)
            }
            ).on("player:pause player:stop", function() {
                a.playing = 
                !1
            }
            ).on("player:fetchend", function(f) {
                a._renderInfo(f);
                a.$open.attr("href", d(b.mp3Url, {
                    id: f
                }))
            }
            );
            l.on("menus.changed", function() {
                a.player.stop();
                a.playing = !1
            }
            )
        },
        _initVolume: function() {
            var a = this
              , b = a.options;
            this.$volSilder.slider({
                value: this.player.getVolume(),
                range: "min",
                slide: function(d, k) {
                    a.player.setVolume(k.value);
                    a.volume = k.value;
                    60 < a.volume ? a.$vol.hasClass(b.volLargeCls) || (a.$vol.addClass(b.volLargeCls),
                    a.$vol.removeClass(b.volMidCls)) : a.$vol.hasClass(b.volMidCls) || (a.$vol.addClass(b.volMidCls),
                    a.$vol.removeClass(b.volLargeCls))
                },
                stop: function(a, b) {
                    $(b.handle).blur()
                }
            })
        },
        _initProcess:function(){
        	var a=this;
        	
            // 注册监听播放进度事件
            a.player.on('timeupdate', timeupdate);
                
        	// 通过jquery-ui的slider组件实现播放进度条的交互
			this.$procSilder.slider({
				range: 'min',
				max: 1000,
				disabled: true,
				start: function() {
					// 为了使拖动操作不受打断，进度条拖动操作开始时即暂停对timeupdate事件的监听
					a.player.off('timeupdate');
				},
				stop: function(d, k) {
					// 拖动结束时再恢复对timeupdate事件的监听
					a.player.play(k.value *a.player.duration());
					
					a.player.on('timeupdate', timeupdate);
					setTimeout(function(){a.player.on('timeupdate', timeupdate);},200);
					
					$(k.handle).blur();
				}
			});
			
			// 监听事件,调整播放进度
			function timeupdate(){
        		var pos = a.player.curPos(),
                duration = a.player.duration();
           		this.$procSilder.slider('option', 'value',duration?pos/duration*1000:0);
			}
        },
        _initPlayer: function(a) {
            var b = this
              , d = b.options;
            $.getScript(d.playerUrl, function() {
                _mu.Player.prototype._fetch = function() {
                    var a, d = this, f = this.getCur();
                    a = $.Deferred();
                    this.getUrl() === f ? a.resolve() : b._getUrlByID(f, function(b) {
                        d.setUrl(b);
                        a.resolve()
                    }
                    );
                    this.trigger("player:fetchend", f);
                    return a.promise()
                }
                ;
                b.player = new _mu.Player({
                    baseDir: d.baseDir,
                    absoluteUrl: !1,
                    volume: b.volume
                });
                
                a && a()
            }
            )
        },
        _getPlayList: function(a) {
            var b = this
              , 
            f = b.options;
            b._cachedJsonp(f.hotUrl, "mp3_hotlist", function(c) {
                var e, g, i;
                if (22E3 == c.error_code) {
                    for (var c = c.song_list, h = 0, j = c.length; h < j; h++)
                        e = c[h],
                        g = e.song_id,
                        i = {},
                        i.name = e.title,
                        i.author = e.author,
                        i.uid = e.ting_uid,
                        b.playList[g] = i,
                        b.player.add(g);
                    0 < j && (c = c[Math.random() * j | 0].song_id,
                    b.player.setCur(c),
                    b._renderInfo(c),
                    b.$open.attr("href", d(f.mp3Url, {
                        id: c
                    })));
                    a && a()
                }
            }
            )
        },
        _getUrlByID: function(a, b) {
            this._cachedJsonp(this.options.playUrl + "&songid=" + a, "mp3_play_" + a, function(a) {
                22E3 == a.error_code && (a = HAO.httpsTrans(a.bitrate.file_link),
                b && b(a))
            }
            )
        },
        _renderInfo: function(a) {
            var b = this.options
              , c = this.playList[a];
            c && (a = $(d(b.tpl, {
                songid: a,
                uid: c.uid,
                name: c.name,
                author: c.author
            })),
            this.$info.children().remove(),
            this.$info.append(a),
            a.marquee())
        },
        _cachedJsonp: function(a, b, c) {
            var d = window[b];
            window[b] = function(a) {
                c && c(a)
            }
            ;
            $.ajax({
                dataType: "script",
                url: a + ("&callback=" + b),
                cache: !0,
                success: function() {
                    window[b] = d;
                    if (void 0 === d)
                        try {
                            delete window[b]
                        } catch (a) {}
                }
            })
        }
    });
    return e
}
);
__d("widget.imagelist.imagelist", ["common.js.widget"], function(g, h, e, c) {
    c("common.js.widget");
    $.widget("hao123.imagelist", {
        options: {},
        _create: function() {
            this.$pics = $(".item-hook", this.element);
            this._bindEvent()
        },
        _init: function() {},
        _bindEvent: function() {
            this.$pics.hover(function() {
                $(this).find(".cover-hook").show()
            }
            , function() {
                $(this).find(".cover-hook").hide()
            }
            )
        },
        destroy: function() {
            $.Widget.prototype.destroy.call(this)
        }
    });
    return e
}
);
__d("widget.gamepicture.gamepicture", ["common.js.widget"], function(g, h, e, c) {
    c("common.js.widget");
    $.widget("hao123.gamepicture", {
        options: {},
        _create: function() {
            this.$pics = $(".item-hook", this.element);
            this._bindEvent()
        },
        _init: function() {},
        _bindEvent: function() {
            var c = this.element;
            $(c).hover(function() {
                $(c).find(".cover-hook").show()
            }
            , function() {
                $(c).find(".cover-hook").hide()
            }
            )
        },
        destroy: function() {
            $.Widget.prototype.destroy.call(this)
        }
    });
    return e
}
);
__d("widget.sidelink2.sidelink2", ["common.js.widget"], function(g, h, e, c) {
    c("common.js.widget");
    $.widget("hao123.sidelink2", {
        _create: function() {
            this._changeStyle()
        },
        _changeStyle: function() {
            $("li", this.element).each(function() {
                0 < $(this).find(".singleone").length && $(this).children("a").addClass("singlelink")
            }
            )
        },
        destroy: function() {
            $.Widget.prototype.destroy.call(this)
        }
    });
    return e
}
);
window.js_HJoPOdorJq && window.js_HJoPOdorJq(!0);
