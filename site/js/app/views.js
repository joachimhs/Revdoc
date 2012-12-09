BookReview.ApplicationView = Ember.View.extend({
    elementId: 'mainArea',
    templateName: 'application'
});

BookReview.LoginView = Ember.View.extend({
    elementId: 'loginArea',
    templateName: 'login-page'
});

BookReview.DocumentsView = Ember.View.extend({
    elementId: 'documentsArea',
    templateName: 'documents-page'
});

BookReview.ShowDocumentView = Ember.View.extend({
    elementId: 'showDocumentsArea',
    templateName: 'show-documents-page'
});

BookReview.ChaptersView = Ember.View.extend({
    elementId: 'chaptersArea',
    templateName: 'chapters-page'
});

BookReview.ChapterView = Ember.View.extend({
    elementId: 'chapterArea',
    templateName: 'chapter-page'
});

BookReview.FileUploadView = Ember.View.extend({
    tagName: 'div',
    //template: Ember.Handlebars.compile('<input id="fileupload" type="file" name="files[]" data-url="/fileupload" multiple><button {{action uploadFile target="this"}}>Upload</button>'),


    didInsertElement: function() {
        $('#' + this.get('elementId')).fineUploader({
            request: {
                endpoint: 'uploadFile?documentId=' + BookReview.router.showDocumentController.get('id')
            },
            text: {
                uploadButton: 'Attach file to Document'
            },
            debug: true
        });
    }
});