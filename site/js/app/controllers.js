BookReview.ApplicationController = Ember.Controller.extend({
    init: function() {
        BookReview.log('Application Controller: init');
    }
});

BookReview.DocumentsController = Ember.ArrayController.extend({
    content: [],
    newDocumentTitle: null
});

BookReview.ShowDocumentController = Ember.ObjectController.extend({
    content: null,
    newChapterTitle: null,

    getNextIndex: function() {
        var index = 1;

        var chapters = this.get('content.chapters');
        if (chapters && chapters.get('length') >= 1) {
            index = chapters.get('length') + 1;
        }

        return index;
    }
});

BookReview.ChaptersController = Ember.ArrayController.extend({
    content: []
});

BookReview.ChapterController = Ember.ObjectController.extend({
    content: null
});

BookReview.ChapterSectionsController = Ember.ArrayController.extend({
    contentBinding: 'chapterController.content.sections',
    chapterController: null,
    sortProperties: ['sectionIndex']
});

BookReview.ParagraphsProxy = Ember.ArrayController.extend({
    content: [],
    sortProperties: ['paragraphIndex']
});
