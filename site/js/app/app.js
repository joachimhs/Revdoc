Ember.ENV.RAISE_ON_DEPRECATION = true;

var BookReview = Ember.Application.create({
    log: function(message) {
        if (window.console) console.log(message);
    }
});

//Removing the Camelcase-to-dash convention from Ember Data
DS.Model.reopen({
    namingConvention: {
        keyToJSONKey: function(key) {
            return key;
        },

        foreignKey: function(key) {
            return key;
        }
    }
});

DS.Model.reopen({
    reload: function() {
        if (!this.get('isDirty') && this.get('isLoaded')) {
            var store = this.get('store'),
                adapter = store.get('adapter');

            adapter.find(store, this.constructor, this.get('id'));
        }
    }
});

BookReview.Serializer = DS.JSONSerializer.extend({
    addBelongsTo: function(hash, record, key, relationship) {
        hash[key] = record.get(key + ".id");
    }

    /*addHasMany: function(hash, record, key, relationship) {
        hash[key] = record.get(key).getEach('id');
    }*/
});

BookReview.Adapter = DS.Adapter.create({
    serializer: BookReview.Serializer.create(),

    extractChapterFromJson: function(type, data, store) {
        if (data.abstracts) store.loadMany(BookReview.ChapterAbstract, data.abstracts);
        if (data.paragraphs) store.loadMany(BookReview.ChapterParagraph, data.paragraphs);
        if (data.sections) store.loadMany(BookReview.ChapterSection, data.sections);
        if (data.figures) store.loadMany(BookReview.ChapterParagraphFigure, data.figures);
        if (data.examples) store.loadMany(BookReview.ChapterParagraphExample, data.examples);
        if (data.comments) store.loadMany(BookReview.ChapterParagraphComment, data.comments);
        if (data.chapters) store.loadMany(type, data.chapters);
    },

    findAll: function(store, type) {
        var url = type.url;

        BookReview.log('finding all: type: ' + type + ' url: ' + url);
        var adapter = this;
        $.ajax({
            type: 'GET',
            url: url + 's',
            contentType: 'application/json',
            success: function(data) {
                BookReview.log(type);
                if (type === BookReview.Chapter) {
                    adapter.extractChapterFromJson(type, data, store);
                } else if (type === BookReview.Document) {
                    if (data.documents) store.loadMany(type, data.documents);
                } else {
                    store.loadMany(type, data);
                }
            }
        });

    },
    
    find: function(store, type, id) {
        if (type === BookReview.Document) {
            this.findAll(store, type);
            return;
        }
        var url = type.url;

        BookReview.log('finding: type: ' + type + ' url: ' + url + ' id: ' + id);

        var requestStringJson = {

        };

        if (type === BookReview.Chapter) {
            requestStringJson.chapterId = id;
            requestStringJson.documentId = BookReview.router.showDocumentController.get('content.id');
        }

        var adapter = this;

        $.ajax({
      	  type: 'GET',
      	  url: url,
      	  data: JSON.stringify(requestStringJson, null, '\t').replace(/\%/g,'%25'),
      	  contentType: 'application/json',
      	  success: function(data) {
                if (type === BookReview.Chapter) {
                    adapter.extractChapterFromJson(type, data, store);
                } else {
                BookReview.store.load(type, data); }
            }
      	});
    },

    findQuery: function(store, type, query, modelArray) {
        BookReview.log('FINDQUERY');
        BookReview.log(query);
        BookReview.log(query.id);
        BookReview.log(modelArray);

    },

    updateRecord: function(store, type, model) {
        var url = type.url;

        BookReview.log('updating record: type: ' + type + ' id: ' + model.get('id') + ' url: ' + url);
        BookReview.log('json: ' + JSON.stringify(model.serialize({ includeId: true })));

        jQuery.ajax({
            url: url,
            data: JSON.stringify(model.serialize({ includeId: true })),
            dataType: 'json',
            type: 'PUT',

            success: function(data) {
                // data is a hash of key/value pairs representing the record
                // in its current state on the server.
                BookReview.log('got back from updateRecord type: ' + type + " id: " + model.get('id') + " ::" + JSON.stringify(data));
                store.didSaveRecord(model, data);
            }
        });
    },

    createRecord: function(store, type, model) {
        var url = type.url;

        BookReview.log('creating record: type: ' + type + ' id: ' + model.get('id') + ' url: ' + url);
        BookReview.log('json: ' + JSON.stringify(model.serialize({ includeId: true })));
        var adapter = this;

        jQuery.ajax({
            url: url,
            data: JSON.stringify(model.serialize({ includeId: true })),
            dataType: 'json',
            type: 'POST',

            success: function(data) {
                // data is a hash of key/value pairs representing the record.
                // In general, this hash will contain a new id, which the
                // store will now use to index the record. Future calls to
                // store.find(type, id) will find this record.
                BookReview.log('got back from createRecord type: ' + type + " id: " + model.get('id') + " ::" + JSON.stringify(data));
                store.didSaveRecord(model, data);
            }
        });
    },

    deleteRecord: function(store, type, model) {
        var url = type.url;

        var requestStringJson = {
            id: model.get('id')
        };

        BookReview.log('delting record: type: ' + type + ' id: ' + model.get('id') + ' url: ' + url);
        BookReview.log('json: ' + JSON.stringify(requestStringJson));

        jQuery.ajax({
            url: url,
            dataType: 'json',
            data: JSON.stringify(requestStringJson),
            type: 'DELETE',

            success: function() {
                store.didDeleteRecord(model);
            }
        });
    }
});

//BookReview.Adapter.map('BookReview.Chapter', {
//    abstract: { embedded: "always" }
//});

//EurekaJ.Adapter.map('EurekaJ.AlertModel', { primaryKey: 'alertName' });
//EurekaJ.Adapter.map('EurekaJ.ChartGroupModel', {primaryKey: 'chartGroupName'});

/*DS.RESTAdapter.map('BookReview.Chapter', {
    sections: { embedded: "load" },
    abstr: { embedded: "load" }
});

DS.RESTAdapter.map('BookReview.ChapterAbstract', {
    itemList: { embedded: "load" },
    paragraphs: { embedded: "load" }
});*/

BookReview.ajaxSuccess = function(data) {
    BookReview.Store.loadMany(type, data);
};

BookReview.store = DS.Store.create({
    adapter: BookReview.Adapter,
    //adapter:  DS.RESTAdapter.create({ bulkCommit: false }),
    revision: 10
});