package com.scilari.wavefront

//scalastyle:off
import com.scilari.geometry.models.{DataPoint, Float2}
import org.scalatest.{FlatSpec, Matchers}

class WavefrontTests extends FlatSpec with Matchers {

  def initWf = {
    type P = DataPoint[Float]

    val points = Seq(
      Float2(0, 0),
      Float2(1, 1),
      Float2(2, 2),
      Float2(2, 3)
    ).map{DataPoint[Float](_, Float.PositiveInfinity)}

    points(0).data = 0

    def neighbors(p: P): Seq[P] = {
      val ix = points.indexWhere(_.equalCoordinates(p))
      val nbIx = Seq(ix - 1, ix + 1).filter(i => i >= 0 && i < points.size)
      nbIx.map{points}
    }

    def cost(x: P, y: P) = x.distance(y)

    def getValue(p: P): Float = p.data

    def setValue(p: P, v: Float): Unit = p.data = v

    val wf = new Wavefront[P](
      neighbors,
      cost,
      getValue,
      setValue
    )

    (wf, points)
  }

  "Wavefront" should "have correct distances" in {
    val (wf, points) = initWf
    wf.computeCosts(Seq(points(0)))
    points(0).data shouldBe 0f
    points(1).data shouldBe math.sqrt(2).toFloat
    points(2).data shouldBe 2*math.sqrt(2).toFloat
    points(3).data shouldBe 2*math.sqrt(2).toFloat + 1
  }

  it should "return path from start to goal" in {
    val (wf, points) = initWf
    wf.computeCosts(Seq(points(0)))
    val path = wf.followGradient(points(3))
    path should have size 4
    path should contain theSameElementsInOrderAs points.reverse
  }


}
