# Brainframe

The aim of this project is to create a one-stop-shop zettelkasten inspired
notes, handwriting & drawing, and diagraming tool.

## Constraints

- Must be open-source, users should be able to run it themselves, modify it, and
  own their data.
- Text-file based, so entries are editor independent
- Readily hackable. User's should be able to encode custom content types they
  can use or submit upstream.

## Why?

Currently, I'm spitting up all my notes between several different systems:

### Notion

I've been using Notion since late 2015, and created a zettelkasten around 2020.
I like the web UI as I do a lot of drafting from mobile devices. Its editor
interface strikes a good balance between flexibility and ease of use. For
example, organizing all content into blocks makes it easy to shuffle around. Its
database features are also quite useful, which is what I used to make my own
zettelkasten, I could add a nice amount of structure so that I had fields for
links from a note to a reference, or the next, parent, and previous notes.

The downsides are that Notion owns your data, and you can only access it through
them. If the company gets acquired, goes out of business, or encounters a
catastrophic failure, there are no gurantees you will get your work back. The
other huge downside is that it's just not quite extensible enough, with the
recent introduction of their APIs it is more extensible than it used to be, but
there's not really a path for defining custom block types.

### Miro

My goto for diagramming. Another cloud service, it's not the best UX out there,
but for diagrams and especially taking realtime visual notes, it definitely does
the job. It's got a plethora of nice stuctural elements destop users can use
with a mouse & keyboard, and an OK tablet drawing experience.

Unfortunately it's not very customizeable, you can style a lot of elements but
you can't define a custom theme. Additionally, the diagrams themselves are not
text-based meaning there is not a readily available way to generate diagrams
programatically. Lastly, the tablet drawing experience is not the best, it
doesn't use a lot of common platform shortcuts, and things like straight lines
require switching tools as opposed to holding down the pencil at the end of a
stroke.

### Goodnotes 6

My goto for journaling, writing, and doodling. The drawing tools are pretty
decent and the UX is very intuitive, this tool I use extensively every day. Not
too many complaints other than it doesn't support structural editing at all. So
there is no taking it to a desktop to draw boxes, add text labels, connect
elements with nodes. It's also closed source and can only export to image or
pdf. Additionally, it doesn't support an infinite canvas so you have to use
either the default paper sizes and styles or generate your own pdfs to import.

### Concepts

Another vector drawing app I use regularly. I use it for drawing more technical
elements like wood projects or floorplans given Concept's precision, scale, and
measurement tools. It heavily features a puck UI where you can assign tools
around it and customize them for easy access. It's got some custom grid options
and an intinite canvas, making it ideal for capturing project requirements or
brainstorming on a single page. The downsides are not much structual editing,
but more than Goodnotes, and the colors are limited to the copic marker palette,
which is kind of unnecessary for my needs. Also the grid customization could be
pushed further as I prefer a subgrid for lowercase words, basegrid for capital
letters and general spacing and layout, and a dotgrid on top for drawing.

### Org

While I have not been using this as much lately since switching to neovim, there
are a lot of good ideas and community packages that make it a decent platform.
My main sticking points is that it's very tightly tied to emacs itself and all
the other text-editor implementations of org pale in comparison. This is also
why there are not really great options for writing on mobile devices or tablets.
Solutions exist but they feel more like work arounds than full solutions. It
also does not readily enable fields or validation. Let's say you're trying to
create a books database, there's not many tools within reach to enforce an
author field for each entry. Of course emacs being so flexible, one could write
validations it's still very ad-hoc and would take a lot of work to fully
customize.

## The Vision

### Local Files

Currently, there are several types of notes I would like to permenently store.

1. Inbox for rough notes that need more details at a later time.
2. References for things like articles, books, tools, or articles I would
   possibly write notes on.
3. Zettelkasten notes for permanent storage. Full ideas or documents, typically
   linked to other notes or references. Should always make sense on their own.
4. Documents - Used for polished complete documents that may or may not include
   links to references or other zettelkasten notes.
5. Projects for documenting a larger project and its subtasks.
6. Journal for daily entries and free writing.
7. Drafts for articles not ready to be published on my personal site.
8. Articles that are ready to be published on my personal site.

To support those content types my inclination is to support having different
folders for each type of content. That should make it intuitive to organize on a
wide range of systems, and be flexible enough for other users to create their
own content types and extend with specs or other validation rules.

### Clojure, Hiccup vector style

Each note file would be a valid clojue-supported hiccup style edn document. The
following is a rough draft of the idea:

```clojure
[:note
 {:title "An example of a note"
  :tags [:example :brainstorming :project]}
 [:p "This is the body of a note."]
 [:code :clojure "(+ 1 2)"]]
```

1. No need to write a custom parser. Clojure's `read-string` should work quite
   well for starting out.
2. Rigid structure means that fields like links, code, tags, etc... and even
   document types can be more readily validated.
3. Hiccup syntax quickly became extremely thoughtless to write, so it dosen't
   get in the way.
4. Tools like treesitter support overrides so that it should be pretty painless
   to make the `:code` element support syntax highlighting.
5. Because the file is already encoded as a tree it should be practical to
   register handlers for each element type and render it depending on the
   context. In other words, each element is only intended to capture an
   intention of the content type. It would be another system's job to translate
   it into components to render in a web view. It also means if you have an
   element like `:todos` one could register a handler to transform each child
   element as a todo item and display a checkbox if you don't want an explicit
   todo item type. Either way, we know the relationship and the intention of
   each element as soon as it's parsed.
6. Because of the structural format, it's ok to define new element types when a
   handler does not exist. For example if I wanted mermaid charts, I could use
   an element like `[:mermaid "a->b"]` then on a web view it would be treated as
   plain text until I write a handler to render it through mermaid.
7. While not as text-editor friendly as markdown or org, I'm anticipating that I
   would mostly be writing and editing notes from the web ui. That way it can
   support the critical tablet drawing, diagramming, and rendering dropdowns for
   fields like tags or project states.

### A database and watcher process

Similar to org-roam, I like the idea of making my note files the source of truth
then having a watcher process react to filesystem changes to process a file and
track links, titles, tags, and other helptul meta data in a sqlite or postgres
db. This can power smarter autocompletion later but immediately be used to power
searches and backlinks without expensive pocessing at runtime of all notes.

### Tablet Drawing

The ability to draw on a tablet or drawing pad is a critical feature to this
project. I like thinking and journaling through Goodnotes as a starting point,
then type it up and structure the notes in Notion, then use tools like Miro to
create diagrams or similar visuals.

Similar to Goodnotes 6, I don't want too many drawing tools but instead rely on
gestures like leaving the stylus on the screen at the end of a stroke to
trantform it into a line or a shape.

Unlike Goodnotes 6, I would like to be able to modify drawing elements after the
fact like bezier curves and style properties on desktop with a mouse and
keyboard.

Drawings should be created from the web ui which needs to function well on
mobile devices like tablets. It needs to use common built-in shortcuts like
double tapping the stylus to switch to the previous tool, as well as three
finger and four finger taps to redo\undo. Ultimately, one should be able to get
a page done with at least the main vector drawing tool. Handwriting and
sketching will also be captured as hiccup vectors that loosely resemble svg
elements. Most lines and shapes will emulate the path svg element, but may be
simplified to just a collection of points in a vector. When holding down a
stroke to transform into a shape, the web ui should find the closest shape, and
switch elements to the best fit. The original points should still be stored in
the new element. This allows users to change shapes after the fact. With raw
point sequences this will support being able to adjust the smoothness in the
lines connecting the dots.

### Diagramming

After I suffciently capture an idea in the tablet drawing features, there are
many times I would like to further polish my sketches into more proffessional
diagrams. This entails composing shapes, modifying properties, connecting
elements with lines, and adding labels.

This too should generate hiccup elements from a web ui that encode the shape and
their connections. It should also be possible to make an element link to another
document regardless of content type. So if I have a kanban board of projects I
would like to make each project a like to their corresponding project files.
