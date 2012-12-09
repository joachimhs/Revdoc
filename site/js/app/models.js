BookReview.Document = DS.Model.extend({
    title: DS.attr('string'),
    documentOwnerUserId: DS.attr('string'),
    chapters: DS.hasMany('BookReview.Chapter'),
    attachments: DS.attr('string'),

    attachmentsArray: function() {
        if (this.get('attachments')) {
            return this.get('attachments').split(',');
        }

        return [];
    }.property('attachments')
});

BookReview.Document.reopenClass({
    url: "document"
});

BookReview.Chapter = DS.Model.extend({
    document: DS.belongsTo('BookReview.Document'),
    title: DS.attr('string'),
    index: DS.attr('number'),
    abstract: DS.belongsTo('BookReview.ChapterAbstract'),
    sections: DS.hasMany('BookReview.ChapterSection')
});

BookReview.Chapter.reopenClass({
    url: "chapter"
});

BookReview.ChapterAbstract = DS.Model.extend({
    chapter: DS.belongsTo('BookReview.Chapter'),
    itemList: DS.hasMany('BookReview.ChapterParagraph'),
    paragraphs: DS.hasMany('BookReview.ChapterParagraph')
});

BookReview.ChapterAbstract.reopenClass({
    url: "abstract"
});

BookReview.ChapterSection = DS.Model.extend({
    chapter: DS.belongsTo('BookReview.Chapter'),
    type: DS.attr('string'),
    title: DS.attr('string'),
    sectionIndex: DS.attr('number'),
    paragraphs: DS.hasMany('BookReview.ChapterParagraph'),
    subSections: DS.hasMany('BookReview.ChapterSection'),

    sortedParagraphs: function() {
        console.log('BookReview.ChapterSection sortedParagraphs');
        var paragraphs = this.get('paragraphs');
        return BookReview.ParagraphsProxy.create({content: paragraphs} )
    }.property('paragraphs.@each').cacheable(),

    hasHeader: function() {
        var title = this.get('title');

        return title
    }.property('title'),

    addParagraphAtIndex: function(index) {
        console.log('Adding paragraph at index: ' + index);
        var currSection = this;
        this.get('paragraphs').forEach(function(paragraph) {
            if (paragraph.get('paragraphIndex') > index) {
                var newParagraphIndex = paragraph.get('paragraphIndex') + 1;
                paragraph.set('paragraphIndex', newParagraphIndex);
            }
        });

        var paragraph = BookReview.ChapterParagraph.createRecord({
            id: Math.uuid(16, 16),
            content: '',
            paragraphIndex: index,
            chapterSection: currSection
        });

        this.get('paragraphs').pushObject(paragraph);
    }
});

BookReview.ChapterSection.reopenClass({
    url: "section"
});

BookReview.ChapterParagraph = DS.Model.extend({
    chapterSection: DS.belongsTo('BookReview.ChapterSection'),
    content: DS.attr('string'),
    comments: DS.hasMany('BookReview.ChapterParagraphComment'),
    figure: DS.belongsTo('BookReview.ChapterParagraphFigure'),
    example: DS.belongsTo('BookReview.ChapterParagraphExample'),
    paragraphIndex: DS.attr('number'),
    isEditing: false,

    numComments: function() {
        var comments = this.get('comments');
        if (comments) {
            return comments.get('length');
        }

        return 0;
    }.property('comments.length'),

    isList: function() {
        return this.get('content').trim().indexOf("-") === 0;
    }.property('content').cacheable(),

    listContent: function() {
        if (this.get('isList')) {
            var list = "";
            var lines = this.get('content').split('\n');
            lines.forEach(function(line) {
                line = line.trim();
                if (line.indexOf("-") === 0) {
                    list += '<li>' + line.slice(1) + '</li>';
                }
            });
            return new Handlebars.SafeString('<ul>' + list+ '</ul>');
        }

        return this.get('content');
    }.property('isList').cacheable()
});

BookReview.ChapterParagraphComment = DS.Model.extend({
    documentId: DS.attr('string'),
    chapterId: DS.attr('string'),
    chapterParagraph: DS.belongsTo('BookReview.ChapterParagraph'),
    commentFrom: DS.attr('string'),
    commentDate: DS.attr('date'),
    commentContent: DS.attr('string')
});

BookReview.ChapterParagraphComment.reopenClass({
    url: "paragraphComment"
});

BookReview.ChapterParagraphFigure = DS.Model.extend({
    chapterParagraph: DS.belongsTo('BookReview.ChapterParagraph'),
    title: DS.attr('string'),
    link: DS.attr('string')
});

BookReview.ChapterParagraphExample = DS.Model.extend({
    chapterParagraph: DS.belongsTo('BookReview.ChapterParagraph'),
    title: DS.attr('string'),
    link: DS.attr('string'),

    linkObserver: function() {
        var model = this;

        console.log(this.get('link'));
        $.get("/" + this.get('link'), function(data) {
            console.log(data);
            model.set('content', data);
        }, "text")
        .error(function() {
            model.set('content', "Unable to find example file.");
            //TODO: Navigate to 404 state
        });
    }.observes('link')
});