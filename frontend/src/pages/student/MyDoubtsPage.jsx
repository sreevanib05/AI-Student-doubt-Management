import { useEffect, useState } from 'react';
import DoubtTable from '../../components/DoubtTable.jsx';
import EmptyState from '../../components/EmptyState.jsx';
import ErrorAlert from '../../components/ErrorAlert.jsx';
import PageHeader from '../../components/PageHeader.jsx';
import { getApiErrorMessage } from '../../services/api.js';
import { doubtService } from '../../services/doubtService.js';

export default function MyDoubtsPage() {
  const [doubts, setDoubts] = useState([]);
  const [error, setError] = useState('');

  useEffect(() => {
    doubtService.myDoubts()
      .then(setDoubts)
      .catch((exception) => setError(getApiErrorMessage(exception, 'Could not load doubts.')));
  }, []);

  return (
    <>
      <PageHeader title="My Doubts" description="Full history of your submitted doubts and mentor responses." />
      <ErrorAlert message={error} />
      <div className="mt-6">
        {doubts.length > 0 ? (
          <DoubtTable doubts={doubts} />
        ) : (
          <EmptyState title="No doubts found" description="Submit a doubt to start tracking it here." />
        )}
      </div>
    </>
  );
}
