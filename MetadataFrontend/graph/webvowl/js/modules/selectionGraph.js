module.exports = function () {

    var selection = {},
        listeners = [];
        all = [];
        selected = {};
        map = {};


    selection.filteredProperties = function () {
        return filteredProperties;
    };

    selection.listen = function(callback) {
        listeners.push(callback);
    };
    selection.notify = function() {
        listeners.map(function(listener) {
            listener();
        });
    };
    selection.size = function() {
        return all.length;
    };
    selection.add = function(node, role) {
        role = 'selected';
        if (!(node.id() in map)) {
            all.push(node);
        }
        map[node.id()] = node;
        if (role == 'selected') {
            selected[node.id()] = node;
        }
        this.notify();
    };
    selection.is_selected = function(node) {
        return node.id() in map;
    };
    selection.clear = function() {
        selected = {};
        all = [];
        map = {};
        this.notify();
    };
    selection.clear_contacts = function() {
        all = [];

        for (var sel in selected) {
            all.push(selected[sel]);
        }

        this.notify();
    };
    selection.selected = function() {
        return selected;
    };
    selection.all = function() {
        return all;
    };

    return selection;
};
