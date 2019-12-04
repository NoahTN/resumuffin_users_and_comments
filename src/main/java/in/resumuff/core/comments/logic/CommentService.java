package in.resumuff.core.comments.logic;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import in.resumuff.core.comments.db.CommentRepository;
import in.resumuff.core.comments.entities.Comment;
import in.resumuff.core.users.db.RoleRepository;
import in.resumuff.core.users.db.UserRepository;
import in.resumuff.core.users.entities.User;

@Service
public class CommentService {
    @Autowired
    private CommentRepository commentRepo;

    @Autowired
    private UserRepository userRepo;

    public Comment getComment(long id) {
        return commentRepo.findById(id).orElse(null);
    }

    public Comment getThread(long threadId) {
        return commentRepo.findByThreadId(threadId).orElse(null);
    }

	public ArrayList<Comment> getTopTenThreads() {
		return commentRepo.findTop10By();
	}

	public void createThread(HttpSession session, long userId, long resumeId, String subject, String content) {
        if(session.getAttribute("TOKEN") != null) {
            User user = userRepo.findById(userId).orElse(null);
            if(user != null) {
                if(user.getAccessToken().equals(session.getAttribute("TOKEN")) && user.getRole().getSTART_NEW_THREAD()) {
                    Comment comment = new Comment(userId, resumeId, subject, content);
                    commentRepo.save(comment);
                }
            }
        }
	}

	public void createComment(HttpSession session, long userId, long parentId, long threadId, String content) {
        if(session.getAttribute("TOKEN") != null) {
            User user = userRepo.findById(userId).orElse(null);
            if(user != null) {
                if(user.getAccessToken().equals(session.getAttribute("TOKEN")) && user.getRole().getPOST_COMMENTS()) {
                    Comment comment = new Comment(userId, parentId, threadId, content);
                    commentRepo.save(comment);
                }
            }
        }
	}

	public void deleteComment(HttpSession session, long id, long userId) {
        if(session.getAttribute("TOKEN") != null) {
            Comment comment = commentRepo.findById(id).orElse(null);
            if(comment != null) {
                User user = userRepo.findById(userId).orElse(null);
                if(user != null && user.getAccessToken().equals(session.getAttribute("TOKEN"))) {
                    if(comment.getUserId() == userId || user.getRole().getDELETE_COMMENTS()) {
                        commentRepo.deleteById(id);
                    }
                }
            }
        }
	}

	public void upvoteComment(HttpSession session, long id) {
        if(session.getAttribute("TOKEN") != null) {
            Comment comment = commentRepo.findById(id).orElse(null);
            if(comment != null) {
                comment.setUpvotes(comment.getUpvotes()+1);
                commentRepo.save(comment);
            }
        }
	}

	public void downvoteComment(HttpSession session, long id) {
        if(session.getAttribute("TOKEN") != null) {
            Comment comment = commentRepo.findById(id).orElse(null);
            if(comment != null) {
                comment.setDownvotes(comment.getDownvotes()+1);
                commentRepo.save(comment);
            }
        }
	}
}