package com.scilari.wavefront

//scalastyle:off
import java.awt.{Color, Font, Graphics}
import java.awt.event.{MouseEvent, MouseListener, MouseMotionListener}

import com.scilari.geometry.models.{DataPoint, Float2}
import com.scilari.geometry.plotting.Panels.Panel
import com.scilari.geometry.plotting._
import com.scilari.geometry.spatialsearch.quadtree.QuadTree

object Demo {

  // point with a cost
  type Point = DataPoint[Float]

  def main(args: Array[String]): Unit ={
    val points = spiral(pointCount = 5000, width = 1000f).map{DataPoint(_, 0f)}
    val pathPanel = new PathPanel(1000, 1000, QuadTree(points))
    new Panels.Frame("Wavefront Demo", pathPanel)
  }


  class PathPanel(w: Int, h: Int, tree: QuadTree[Point]) extends Panel(w, h) with MouseMotionListener with MouseListener{
    setBackground(new Color(0f, 0f, 0f))
    setOpaque(false)
    addMouseMotionListener(this)
    addMouseListener(this)
    var targetPoint: Point = tree.knnSearch(Float2(w/2, w/2), 1).head
    var path: Seq[Point] = Nil

    val points: Seq[Point] = tree.elements

    val neighborRadius = 20f
    val wf = new Wavefront[Point](
      p => tree.rangeSearch(p, neighborRadius),
      (p1, p2) => p1.distance(p2),
      p => p.data,
      (p, x) => p.data = x
    )

    wf.computeCosts(List(targetPoint))

    var beginningPoint: Point = tree.knnSearch(Float2.zero, 1).head

    def mouseMoved(e: MouseEvent){
      val queryPoint = Float2(e.getX, e.getY)
      beginningPoint = tree.knnSearch(queryPoint, 1).head
      path = wf.followGradient(beginningPoint)
      repaint()
    }

    def mouseDragged(e: MouseEvent){}
    def mousePressed(e: MouseEvent){
      val queryPoint = Float2(e.getX, e.getY)
      tree.foreach(_.data = Float.PositiveInfinity)
      targetPoint = tree.knnSearch(queryPoint, 1).head
      targetPoint.data = 0f
      wf.computeCosts(List(targetPoint))
    }

    def mouseReleased(e: MouseEvent){}
    def mouseClicked(e: MouseEvent){}
    def mouseEntered(e: MouseEvent){}
    def mouseExited(e: MouseEvent){}

    override def paintComponent(g: Graphics){
      super.paintComponent(g)
      g.setColor(Color.BLACK)
      g.fillRect(0, 0, w, h)
      drawPoints(points, Color.RED, 2)(g2d)
      drawPoints(path, Color.GREEN, 10)(g2d)

      g2d.setFont(new Font("TimesRoman", Font.PLAIN, 26))
      g2d.drawString("Path length: " + beginningPoint.data.toInt , 10, 30)
    }
  }

  def spiral(pointCount: Int, width: Float): Array[Float2] = {
    val rounds = 3.0f
    val halfWidth = 0.5f*width
    val maxR = 0.8f*halfWidth
    val angles = (0.0 until 2*math.Pi*rounds by 2*math.Pi*rounds/pointCount).toArray
    val sines = angles.map(math.sin)
    val cosines = angles.map(math.cos)
    val radii = (0f until maxR by maxR/pointCount).toArray
    val points = (cosines, sines, radii).zipped map{(c, s, r) => new Float2(halfWidth + r*c, halfWidth + r*s)}
    points.map{p => p + Float2.random*0.12f*halfWidth}
  }

}
