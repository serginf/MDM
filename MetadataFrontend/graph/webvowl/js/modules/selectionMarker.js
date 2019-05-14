// https://github.com/pcbje/ggraph
module.exports = function () {

    var marker = {},
        poly,
        enabled = false,
        points = [],
        markedFunct,
        releaseFunct,
        state = {select: true, started: false};

    marker.enabled = function (p) {
        if (!arguments.length) return enabled;
        enabled = p;
        return marker;
    };


    // https://github.com/Jam3/chaikin-smooth
    var smooth = function(input, output) {
        if (!Array.isArray(output))
            output = []

        if (input.length>0 && input[0]) {
            output.push([input[0][0], input[0][1]])
        }
        for (var i=0; i<input.length-1; i++) {
            var p0 = input[i]
            var p1 = input[i+1]
            var p0x = p0[0],
                p0y = p0[1],
                p1x = p1[0],
                p1y = p1[1]

            var Q = [ 0.75 * p0x + 0.25 * p1x, 0.75 * p0y + 0.25 * p1y ]
            var R = [ 0.25 * p0x + 0.75 * p1x, 0.25 * p0y + 0.75 * p1y ]
            output.push(Q)
            output.push(R)
        }
        if (input.length > 1) {
            output.push([input[input.length-1][0], input[input.length-1][1]])

        }

        return output
    }

    marker.contains = function(point) {
        var vs = marker.points;
        var x = point[0], y = point[1];
        var inside = false;
        for (var i = 0, j = vs.length - 1; i < vs.length; j = i++) {
            var xi = vs[i][0], yi = vs[i][1];
            var xj = vs[j][0], yj = vs[j][1];
            var intersect = ((yi > y) != (yj > y))
                && (x < (xj - xi) * (y - yi) / (yj - yi) + xi);
            if (intersect) inside = !inside;
        }
        return inside;
    };

    function to_str(points, skip, do_smooth) {
        var arr = [];

        var x = [];
        points.map(function(a,i ) {
            if (i % skip == 0) x.push(a)
        });

        x.push(points[points.length - 1]);

        if (do_smooth) {
            x = smooth(x);
        }
        x.map(function(point) {
            arr.push(point[0] + ',' + point[1])
        })

        return arr.join(' ')
    }

    marker.marked = function(callback) {
        // callbacks.push(callback);
        markedFunct =callback;
    };

    marker.afterMarked = function(f){
        releaseFunct =f;
    };


    var call = function(clear) {
        if(markedFunct)
            markedFunct(clear);
    };

    marker.init = function(container, background, _offset_x, _offset_y) {
        poly = container.append('polygon').attr('class', 'polygonMarker');
        offset_x = _offset_x;
        offset_y = _offset_y;
        state.started = false;
        marker.points = [];

        background.on('mousedown', function() {
            if (!state.select) return;
            call(!d3.event.shiftKey);
            state.started = true;
        });

        var up = function() {

            if(releaseFunct && state.started) {
                marker.points = [];
                poly.attr('points', to_str(marker.points, 4, true));
                state.started = false;
                releaseFunct(true);
            }
        };

        document.addEventListener('mouseup', up);
        background.on('mouseup', up);


        background.on('mousemove', function() {
            if (!state.started) return;

            var pt = background.node().createSVGPoint();

            var pos = d3.mouse(background.select("g").node());
            var x = pos[0];
            var y = pos[1];

            marker.points.push([x, y])
            poly.attr('points', to_str(marker.points, 4, true));
            call();
        });


    };

    return marker;

};
