package edu.sabanciuniv.dataMining.data.text.filter;

import java.util.Arrays;

import weka.core.AttributeStats;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.unsupervised.attribute.Remove;
import weka.filters.unsupervised.attribute.RemoveUseless;

public class RemoveInvariant
	extends RemoveUseless {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2296463719397060865L;

	@Override
	public boolean batchFinished() throws Exception {

		if (getInputFormat() == null) {
			throw new IllegalStateException("No input instance format defined");
		}
		if (m_removeFilter == null) {
			// establish attributes to remove from first batch
			Instances toFilter = getInputFormat();
			int[] attsToDelete = new int[toFilter.numAttributes()];
			int numToDelete = 0;
			for (int i = 0; i < toFilter.numAttributes(); i++) {
				if (i == toFilter.classIndex()) {
					continue; // skip class
				}
				AttributeStats stats = toFilter.attributeStats(i);
				if (stats.missingCount == toFilter.numInstances()) {
					attsToDelete[numToDelete++] = i;
				} else if (stats.distinctCount < 2) {
					// remove constant attributes
					attsToDelete[numToDelete++] = i;
				} else if (toFilter.attribute(i).isNominal()) {
					Arrays.sort(stats.nominalCounts);
					
					// remove nominal attributes that are close to being constant
					double variancePercent = (double) stats.nominalCounts[stats.nominalCounts.length-1]
							/ (double) (stats.totalCount - stats.missingCount)
							* 100.0;
					if (variancePercent > m_maxVariancePercentage) {
						attsToDelete[numToDelete++] = i;
					}
				}
			}

			int[] finalAttsToDelete = new int[numToDelete];
			System.arraycopy(attsToDelete, 0, finalAttsToDelete, 0, numToDelete);

			m_removeFilter = new Remove();
			m_removeFilter.setAttributeIndicesArray(finalAttsToDelete);
			m_removeFilter.setInvertSelection(false);
			m_removeFilter.setInputFormat(toFilter);

			for (int i = 0; i < toFilter.numInstances(); i++) {
				m_removeFilter.input(toFilter.instance(i));
			}
			m_removeFilter.batchFinished();

			Instance processed;
			Instances outputDataset = m_removeFilter.getOutputFormat();

			// restore old relation name to hide attribute filter stamp
			outputDataset.setRelationName(toFilter.relationName());

			setOutputFormat(outputDataset);
			while ((processed = m_removeFilter.output()) != null) {
				processed.setDataset(outputDataset);
				push(processed);
			}
		}
		flushInput();

		m_NewBatch = true;
		return (numPendingOutput() != 0);
	}
}