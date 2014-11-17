TriCircle
=========
A watchface for Android Wear, published under the MIT Licence.

This watchface is based on [watchface-template](https://github.com/twotoasters/watchface-template)
by twotoasters but uses a modified version of
[watchface-gears](https://github.com/blalasaadri/watchface-gears).

To modify this watchface you can check it out and set it up with the following commands:

    git clone git@github.com:blalasaadri/tricircle.git <new-watchface-name>
    cd <new-watchface-name>
    git submodule add git@github.com:blalasaadri/watchface-gears.git submodules/watchface-gears

Relevant changes compared to the template were made in the classes
`com.github.blalasaadri.tricircle.widget.Watchface`,
`com.github.blalasaadri.tricircle.widget.ArcView` (which is completely new compared to the
template) and in the `res/layout/watchface.xml` layout file.

Possible modifications via XML
------------------------------
There are a few properties that may be set in the XML file and that change the appearance of the
arcs. A definition of such an arc may look like this:

    <com.github.blalasaadri.tricircle.widget.ArcView
        android:id="@+id/my_arc"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:layout_gravity="center"
        tricircle:angle="0"
        tricircle:lineWidth="2"
        tricircle:handRadius="8"
        tricircle:scale=".74"
        tricircle:color="@color/red" />

The attributes which may be of interest are:

- `tricircle:angle` The initial angle in which the arc is rendered. Can be useful for testing purposes and screenshots.
- `tricircle:lineWidth` The width of the arc line. The default value is 5 (as interpreted by `Paint#setStrokeWidth(float)`)
- `tricircle:handRadius` The radius of the circle at the end of the arc. The default value is 5  (as interpreted by the `RectF` in `Canvas#drawArc(RectF, float, float, boolean, Paint)`)
- `tricircle:scale` The factor of the screen that should be used for the full arc. This is always relative to the minimum of screen height and width.
- `tricircle:color` The color the arc should be drawn in. Both Android color definitions (e.g. `@color/blue`) and hex values (e.g. `#F0F0F0`) are supported.
