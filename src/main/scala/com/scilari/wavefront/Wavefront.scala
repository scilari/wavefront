package com.scilari.wavefront

import scala.annotation.tailrec

/**
  * Implements the wavefront algorithm to find the distances from known node(s) to other nodes.
  *
  * @param neighbors Function to return node neighbors
  * @param cost Cost between two nodes
  * @param getValue Getter for node's current cost value
  * @param setValue Setter for node's updated cost value
  * @tparam T Node type
  */
class Wavefront[T](
  neighbors: T => Seq[T],
  cost: (T, T) => Float,
  getValue: T => Float,
  setValue: (T, Float) => Unit
){

  /**
    * Updates the costs (distances) to all nodes.
    * @param initialPoints The initial nodes that have a known costs (e.g. goal's cost = 0)
    */
  def computeCosts(initialPoints: Seq[T]): Unit = {
    @tailrec
    def update(wavefront: Seq[T]): Unit = {
      if(wavefront.nonEmpty) update(wavefront.flatMap(updateNeighbors))
    }

    def updateNeighbors(point: T): Seq[T] = {
      for{
        neighbor <- neighbors(point)
        newValue = getValue(point) + cost(point, neighbor) if newValue < getValue(neighbor)
      } yield {
        setValue(neighbor, newValue)
        neighbor
      }
    }

    update(initialPoints)
  }

  /**
    * Computes the shortest path to the node with minimal cost.
    * @param point Path starting point.
    * @return Path to the node with minimal cost.
    */
  def followGradient(point: T): Seq[T] = {
    @tailrec
    def nextNeighbor(point: T, acc: Seq[T]): Seq[T] = {
      val lesserNeighbors = neighbors(point).filter(n => getValue(n) < getValue(point))
      if(lesserNeighbors.isEmpty){
        acc
      } else{
        val minNeighbor = lesserNeighbors.minBy(n => getValue(n) + cost(point, n))
        nextNeighbor(minNeighbor, acc :+ minNeighbor)
      }
    }

    nextNeighbor(point, Seq(point))
  }

}

